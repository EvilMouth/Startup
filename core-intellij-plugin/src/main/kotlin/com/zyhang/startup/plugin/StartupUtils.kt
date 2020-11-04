package com.zyhang.startup.plugin

import com.intellij.psi.*
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.psi.KtAnnotationEntry

internal class StartupUtils {
    companion object {
        private const val SHORT_NAME_REGISTER = "StartupTaskRegister"
        private const val NAME_REGISTER = "com.zyhang.startup.$SHORT_NAME_REGISTER"

        fun PsiElement.isStartupTaskRegister(): Boolean {
            return this.toStartupTaskRegister() != null
        }

        fun PsiElement.toStartupTaskRegister(): PsiAnnotation? {
            return when (this) {
                is PsiAnnotation -> this.takeIf { NAME_REGISTER == it.qualifiedName }
                is KtAnnotationEntry -> this.takeIf { SHORT_NAME_REGISTER == it.shortName?.asString() }
                    ?.toLightAnnotation()
                else -> null
            }
        }

        fun PsiAnnotation.parseId(): String {
            return this.findAttributeValue("id")!!.value() ?: throw IllegalStateException("oops, compat issue")
        }

        fun PsiAnnotation.parseIdDependencies(): List<String> {
            return (this.findAttributeValue("idDependencies") as PsiArrayInitializerMemberValue)
                .children
                .filter { psiElement ->
                    psiElement is PsiLiteralExpression || psiElement is PsiReferenceExpression
                }
                .map { psiElement ->
                    psiElement.value() ?: throw IllegalStateException("oops, compat issue")
                }
        }

        private fun PsiElement.value(): String? {
            return when (this) {
                is PsiLiteralExpression -> this.text
                is PsiReferenceExpression -> this.resolve()?.value()
                is PsiField -> this.initializer?.value()
                else -> null
            }
        }
    }
}