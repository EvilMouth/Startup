package com.zyhang.startup.plugin

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.usages.Usage
import com.intellij.usages.UsageInfo2UsageAdapter
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

interface Decider {
    fun shouldShow(usage: Usage): Boolean

    companion object {
        class LinkUpDecider(private val idDependencies: List<String>) : Decider {
            override fun shouldShow(usage: Usage): Boolean {
                val startupTaskRegister = (usage as UsageInfo2UsageAdapter).toStartupTaskRegister()
                if (startupTaskRegister != null) {
                    return idDependencies.contains(startupTaskRegister.parseId())
                }
                return false
            }
        }

        class LinkDownDecider(private val id: String) : Decider {
            override fun shouldShow(usage: Usage): Boolean {
                val startupTaskRegister = (usage as UsageInfo2UsageAdapter).toStartupTaskRegister()
                if (startupTaskRegister != null) {
                    return startupTaskRegister.parseIdDependencies().contains(id)
                }
                return false
            }
        }

        private fun UsageInfo2UsageAdapter.toStartupTaskRegister(): PsiAnnotation? {
            return when (element) {
                is PsiJavaCodeReferenceElement -> element.parent
                is KtNameReferenceExpression -> element.parent.parent.parent.parent
                else -> null
            }?.toStartupTaskRegister()
        }
    }
}