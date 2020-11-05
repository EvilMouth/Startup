package com.zyhang.startup.plugin

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.find.actions.ShowUsagesAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.ui.awt.RelativePoint
import com.zyhang.startup.plugin.StartupUtils.Companion.isStartupTaskRegister
import com.zyhang.startup.plugin.StartupUtils.Companion.parseIdDependencies
import com.zyhang.startup.plugin.StartupUtils.Companion.resolveAnnotationType
import com.zyhang.startup.plugin.StartupUtils.Companion.toStartupTaskRegister
import java.awt.event.MouseEvent

class StartupLinkDependenciesLineMarker : LineMarkerProviderDescriptor(), GutterIconNavigationHandler<PsiElement> {
    override fun getName(): String? = "StartupLineMarker"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {

        if (element.isStartupTaskRegister()) {
            return LineMarkerInfo(
                element,
                element.textRange,
                navigationOnIcon,
                null,
                this,
                GutterIconRenderer.Alignment.LEFT
            )
        }

        return null
    }

    override fun navigate(e: MouseEvent, elt: PsiElement) {
        val startupTaskRegister = elt.toStartupTaskRegister()!!
        val psiClass = startupTaskRegister.resolveAnnotationType()

        ShowUsagesAction().startFindUsages(
            Decider.Companion.LinkDependenciesDecider(startupTaskRegister.parseIdDependencies()),
            psiClass,
            RelativePoint(e),
            PsiEditorUtil.findEditor(psiClass)
        )
    }

    companion object {
        private val navigationOnIcon = IconLoader.getIcon("/icons/up_arrow.svg")
    }
}