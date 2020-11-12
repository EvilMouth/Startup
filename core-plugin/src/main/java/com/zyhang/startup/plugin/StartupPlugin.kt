package com.zyhang.startup.plugin

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.AppExtension
import com.google.common.collect.ImmutableSet
import com.google.gson.Gson
import com.ss.android.ugc.bytex.common.CommonPlugin
import com.ss.android.ugc.bytex.common.Constants
import com.ss.android.ugc.bytex.common.TransformConfiguration
import com.ss.android.ugc.bytex.transformer.TransformEngine
import com.zyhang.startup.plugin.bytex.StartupContext
import com.zyhang.startup.plugin.bytex.StartupExtension
import com.zyhang.startup.plugin.model.StartupInfo
import com.zyhang.startup.plugin.sort.StartupSort
import com.zyhang.startup.plugin.utils.appendLine
import com.zyhang.startup.plugin.utils.redirect
import com.zyhang.startup.plugin.utils.touch
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class StartupPlugin : CommonPlugin<StartupExtension, StartupContext>() {

    companion object {
        private const val TAG = "StartupPlugin"

        private const val CLASS_TARGET_SUFFIX = "__STData"
        private const val PKG = "com/zyhang/startup"
        private const val CLASS_ST_INFO = "$PKG/model/STInfo"
        private const val GEN_PKG = "$PKG/generated"
        private const val CLASS_STARTUP_LOADER_INIT = "$GEN_PKG/StartupLoaderInit"
        private const val CLASS_STARTUP_CORE = "$PKG/StartupCore"
        private const val METHOD_INIT = "init"
        private const val METHOD_REGISTER = "register"
        private const val CLASS_ST_DATA = "$PKG/model/STData"
    }

    private val targetClasses: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())
    private val targetInfoMap: MutableMap<String, StartupInfo> = ConcurrentHashMap()

    override fun getContext(
        project: Project?,
        android: AppExtension?,
        extension: StartupExtension?
    ): StartupContext = StartupContext(project, android, extension)

    override fun traverse(relativePath: String, node: ClassNode) {
        super.traverse(relativePath, node)
        if (node.name.endsWith(CLASS_TARGET_SUFFIX)) {
            targetClasses.add(node.name)

            node.visibleAnnotations?.find {
                it.desc == "L${CLASS_ST_INFO};"
            }?.values?.let {
                val map = hashMapOf<String, Any>()
                for (i in 0 until it.size step 2) {
                    map[it[i] as String] = it[i + 1]
                }
                val info = Gson().fromJson(map["meta"] as String, StartupInfo::class.java)
                targetInfoMap[node.name] = info
            }
        }
    }

    override fun afterTransform(engine: TransformEngine) {
        super.afterTransform(engine)

        if (targetClasses.size != targetInfoMap.size) {
            throw RuntimeException("$TAG something wrong")
        }
        context.logger.i("found target info -> $targetInfoMap")
        val targetInfoList = targetInfoMap.values

        // sort and check
        val sort = StartupSort()

        val allProcess = targetInfoList.map { it.process }.toSet()
        allProcess.forEach { process ->
            val processName = if (process.isEmpty()) "main" else process
            val list = targetInfoList.filter { it.process == process }

            sort.sort(processName, list)
        }

        context.logger.i("startup dispatch order below:" + "\n\n" + sort.generateOrder())
        context.logger.i("startup relationship below:" + "\n\n" + sort.generateRelationship())
        context.logger.i("graphviz dot code below:" + "\n\n" + sort.generateGraphviz() + "\n" + "parse dot code in http://magjac.com/graphviz-visual-editor/")

        // generate loader class
        try {
            // write
            val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
            val cv = object : ClassVisitor(Constants.ASM_API, writer) {}
            cv.visit(
                50,
                Opcodes.ACC_PUBLIC,
                CLASS_STARTUP_LOADER_INIT,
                null,
                "java/lang/Object",
                null
            )
            val mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
                METHOD_INIT,
                "(L${CLASS_STARTUP_CORE};)V",
                null,
                null
            )
            mv.visitCode()
            var labelStart: Label? = null
            for (clazz in targetClasses) {
                val label = Label()
                mv.visitLabel(label)
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                if (targetClasses.indexOf(clazz) == 0) {
                    labelStart = label
                }
                mv.visitTypeInsn(Opcodes.NEW, clazz)
                mv.visitInsn(Opcodes.DUP)
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, clazz, "<init>", "()V", false)
                mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    CLASS_STARTUP_CORE,
                    METHOD_REGISTER,
                    "(L${CLASS_ST_DATA};)V",
                    false
                )
            }
            val labelEnd = Label()
            mv.visitLabel(labelEnd)
            mv.visitLocalVariable(
                "var0",
                "L${CLASS_STARTUP_CORE};",
                null,
                labelStart,
                labelEnd,
                0
            )
            mv.visitMaxs(0, 0)
            mv.visitInsn(Opcodes.RETURN)
            mv.visitEnd()
            cv.visitEnd()

            // output file
            val outputDirPath =
                context.transformContext.invocation.outputProvider.getContentLocation(
                    "StartupGenerated",
                    ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES) as Set<QualifiedContent.ContentType>,
                    ImmutableSet.of(QualifiedContent.Scope.PROJECT),
                    Format.DIRECTORY
                ).absolutePath
            val dest = File(outputDirPath, CLASS_STARTUP_LOADER_INIT + SdkConstants.DOT_CLASS)
            dest.touch()
            writer.toByteArray().redirect(dest)
            context.logger.i("generated file $dest, length ${dest.length()}".also {
                println(it)
            })
        } catch (e: Exception) {
            context.logger.e("generate loader init class error: $e")
        }
    }

    override fun transformConfiguration(): TransformConfiguration {
        return object : TransformConfiguration {
            override fun isIncremental(): Boolean = false
            override fun consumesFeatureJars(): Boolean = extension.isConsumesFeatureJars()
        }
    }
}