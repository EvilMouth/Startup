package com.intellij.find.findUsages;

import com.intellij.find.FindBundle;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.*;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfoToUsageConverter;
import com.intellij.usages.UsageSearcher;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

public final class FindUsagesManager2 {
    private static final Logger LOG = Logger.getInstance(FindUsagesManager2.class);

    public static UsageSearcher createUsageSearcher(@NotNull FindUsagesHandlerBase handler,
                                                    @NotNull PsiElement[] primaryElements,
                                                    @NotNull PsiElement[] secondaryElements, @NotNull FindUsagesOptions findUsagesOptions) {
        return ReadAction.compute(() -> {
            PsiElement2UsageTargetAdapter[] primaryTargets = PsiElement2UsageTargetAdapter.convert(primaryElements);
            PsiElement2UsageTargetAdapter[] secondaryTargets = PsiElement2UsageTargetAdapter.convert(secondaryElements);
            return createUsageSearcher(primaryTargets, secondaryTargets, handler, findUsagesOptions, null);
        });
    }

    public static UsageSearcher createUsageSearcher(@NotNull PsiElement2UsageTargetAdapter[] primaryTargets,
                                                    @NotNull PsiElement2UsageTargetAdapter[] secondaryTargets,
                                                    @NotNull FindUsagesHandlerBase handler,
                                                    @NotNull FindUsagesOptions options,
                                                    PsiFile scopeFile) throws PsiInvalidElementAccessException {
        ReadAction.run(() -> {
            PsiElement[] primaryElements = PsiElement2UsageTargetAdapter.convertToPsiElements(primaryTargets);
            PsiElement[] secondaryElements = PsiElement2UsageTargetAdapter.convertToPsiElements(secondaryTargets);

            ContainerUtil
                    .concat(primaryElements, secondaryElements)
                    .forEach(psi -> {
                        if (psi == null || !psi.isValid()) throw new PsiInvalidElementAccessException(psi);
                    });
        });

        FindUsagesOptions optionsClone = options.clone();
        return processor -> {
            PsiElement[] primaryElements = ReadAction.compute(() -> PsiElement2UsageTargetAdapter.convertToPsiElements(primaryTargets));
            PsiElement[] secondaryElements = ReadAction.compute(() -> PsiElement2UsageTargetAdapter.convertToPsiElements(secondaryTargets));

            Project project = ReadAction.compute(() -> scopeFile != null ? scopeFile.getProject() : primaryTargets[0].getProject());
            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            LOG.assertTrue(indicator != null, "Must run under progress. see ProgressManager.run*");

            ((PsiManagerImpl) PsiManager.getInstance(project)).dropResolveCacheRegularly(indicator);

            if (scopeFile != null) {
                optionsClone.searchScope = new LocalSearchScope(scopeFile);
            }
            Processor<UsageInfo> usageInfoProcessor = new CommonProcessors.UniqueProcessor<>(usageInfo -> {
                Usage usage = ReadAction.compute(() -> UsageInfoToUsageConverter.convert(primaryElements, usageInfo));
                return processor.process(usage);
            });
            Iterable<PsiElement> elements = ContainerUtil.concat(primaryElements, secondaryElements);

            optionsClone.fastTrack = new SearchRequestCollector(new SearchSession());
            if (optionsClone.searchScope instanceof GlobalSearchScope) {
                // we will search in project scope always but warn if some usage is out of scope
                optionsClone.searchScope = optionsClone.searchScope.union(GlobalSearchScope.projectScope(project));
            }
            try {
                for (PsiElement element : elements) {
                    if (!handler.processElementUsages(element, usageInfoProcessor, optionsClone)) {
                        return;
                    }

                    for (CustomUsageSearcher searcher : CustomUsageSearcher.EP_NAME.getExtensionList()) {
                        try {
                            searcher.processElementUsages(element, processor, optionsClone);
                        } catch (IndexNotReadyException e) {
                            DumbService.getInstance(element.getProject()).showDumbModeNotification(
                                    FindBundle.message("notification.find.usages.is.not.available.during.indexing"));
                        } catch (ProcessCanceledException e) {
                            throw e;
                        } catch (Exception e) {
                            LOG.error(e);
                        }
                    }
                }

                PsiSearchHelper.getInstance(project)
                        .processRequests(optionsClone.fastTrack, ref -> {
                            UsageInfo info = ReadAction.compute(() -> {
                                if (!ref.getElement().isValid()) return null;
                                return new UsageInfo(ref);
                            });
                            return info == null || usageInfoProcessor.process(info);
                        });
            } finally {
                optionsClone.fastTrack = null;
            }
        };
    }
}
