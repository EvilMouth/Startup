package com.zyhang.startup.plugin

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
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
    }
}