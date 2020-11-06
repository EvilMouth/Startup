package com.zyhang.startup.plugin

import com.intellij.psi.*
import org.jetbrains.kotlin.asJava.elements.KtLightPsiArrayInitializerMemberValue
import org.jetbrains.kotlin.asJava.elements.KtLightPsiLiteral
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.psi.*

private const val SHORT_NAME_REGISTER = "StartupTaskRegister"
private const val NAME_REGISTER = "com.zyhang.startup.$SHORT_NAME_REGISTER"

fun PsiElement.isStartupTaskRegister(): Boolean {
    return this.toStartupTaskRegister() != null
}

fun PsiElement.toStartupTaskRegister(): PsiAnnotation? {
    return when (this) {
        is PsiAnnotation -> this.takeIf { NAME_REGISTER == it.qualifiedName }
        is KtAnnotationEntry -> this.toLightAnnotation()?.toStartupTaskRegister() // -> PsiAnnotation
        else -> null
    }
}

// must be StartupTaskRegister
fun PsiAnnotation.parseId(): String {
    return this.findAttributeValue("id")!!
        .value()
}

// must be StartupTaskRegister
fun PsiAnnotation.parseIdDependencies(): List<String> {
    val collection = when (val idDependencies = this.findAttributeValue("idDependencies")!!) {
        is KtLightPsiArrayInitializerMemberValue -> {
            idDependencies.kotlinOrigin.children
                .toList()
        }
        is PsiArrayInitializerMemberValue -> {
            idDependencies.children
                .filter { psiElement ->
                    psiElement is PsiLiteralExpression || psiElement is PsiReferenceExpression
                }
        }
        idDependencies.isEmptyDependencies() -> {
            emptyList()
        }
        else -> {
            throw StartupCompatIssue()
        }
    }
    return collection.map { psiElement ->
        psiElement.value()
    }
}

// [] or {}
private fun PsiAnnotationMemberValue.isEmptyDependencies(): PsiAnnotationMemberValue? {
    return this.takeIf { psiElement ->
        psiElement is PsiLiteralExpression &&
                psiElement.text.run {
                    equals("[]") || equals("{}")
                }
    }
}

// fetch field value
// support kotlin
private fun PsiElement.value(): String {
    return when (this) {
        is KtLightPsiLiteral -> {
            this.kotlinOrigin.value() // -> other kt expression
        }
        is PsiLiteralExpression -> {
            this.text
        }
        is PsiReferenceExpression -> {
            this.resolve()!!
                .value() // -> PsiField
        }
        is PsiField -> {
            this.initializer!!
                .value() // -> PsiLiteralExpression
        }
        is KtStringTemplateExpression -> {
            this.text
        }
        isKtExpression() -> {
            ((this as KtElement).resolveToCall()!!
                .resultingDescriptor as VariableDescriptor)
                .compileTimeInitializer!!
                .toString() // use toString() not value(), because other expression use text
        }
        else -> throw StartupCompatIssue()
    }
}

private fun PsiElement.isKtExpression(): PsiElement? {
    return this.takeIf { psiElement ->
        psiElement is KtReferenceExpression || psiElement is KtDotQualifiedExpression
    }
}

// copy from intellij 2020.2.3
fun PsiAnnotation.resolveAnnotationType(): PsiElement {
    val element = nameReferenceElement
    val declaration = element?.resolve()
    if (declaration is PsiClass && declaration.isAnnotationType) {
        return declaration
    } else if (declaration is KtPrimaryConstructor) {
        val parent = declaration.parent
        if (parent is KtClass && parent.isAnnotation()) {
            return parent
        }
    }
    throw StartupCompatIssue()
}
