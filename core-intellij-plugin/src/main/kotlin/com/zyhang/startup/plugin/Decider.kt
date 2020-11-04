package com.zyhang.startup.plugin

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.usages.Usage
import com.intellij.usages.UsageInfo2UsageAdapter
import com.zyhang.startup.plugin.StartupUtils.Companion.parseId
import com.zyhang.startup.plugin.StartupUtils.Companion.parseIdDependencies
import com.zyhang.startup.plugin.StartupUtils.Companion.toStartupTaskRegister

interface Decider {
    fun shouldShow(usage: Usage): Boolean

    companion object {
        class LinkDependenciesDecider(private val idDependencies: List<String>) : Decider {
            override fun shouldShow(usage: Usage): Boolean {
                val element = (usage as UsageInfo2UsageAdapter).element
                val parent = element?.parent
                val startupTaskRegister = parent?.toStartupTaskRegister()
                if (startupTaskRegister != null) {
                    return idDependencies.contains(startupTaskRegister.parseId())
                }
                return false
            }
        }

        class LinkDependentDecider(private val id: String) : Decider {
            override fun shouldShow(usage: Usage): Boolean {
                val element = (usage as UsageInfo2UsageAdapter).element
                val parent = element?.parent
                val startupTaskRegister = parent?.toStartupTaskRegister()
                if (startupTaskRegister != null) {
                    return startupTaskRegister.parseIdDependencies().contains(id)
                }
                return false
            }
        }
    }
}