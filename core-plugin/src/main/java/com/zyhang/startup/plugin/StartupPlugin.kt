package com.zyhang.startup.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.gradle.AppExtension
import com.google.common.collect.ImmutableSet
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ss.android.ugc.bytex.common.CommonPlugin
import com.ss.android.ugc.bytex.common.Constants
import com.ss.android.ugc.bytex.common.TransformConfiguration
import com.ss.android.ugc.bytex.common.visitor.ClassVisitorChain
import com.ss.android.ugc.bytex.transformer.TransformEngine
import com.ss.android.ugc.bytex.transformer.cache.FileData
import com.zyhang.startup.plugin.bytex.StartupContext
import com.zyhang.startup.plugin.bytex.StartupExtension
import com.zyhang.startup.plugin.model.StartupTaskRegisterInfo
import com.zyhang.startup.plugin.sort.StartupSort
import com.zyhang.startup.plugin.utils.redirect
import com.zyhang.startup.plugin.utils.touch
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.concurrent.ConcurrentHashMap

/**
 * 编译期检测启动任务链合法性
 * 包括但不限于
 * 1、id重复
 * 2、依赖循环
 * 3、任务缺失
 * 4、编写正确，需要继承StartupTask并且注解StartupTaskRegister
 */
class StartupPlugin : CommonPlugin<StartupExtension, StartupContext>() {

    companion object {
        private const val PKG = "com/zyhang/startup"
        private const val GEN_PKG = "$PKG/generated"
        private const val CLASS_STARTUP_LOADER_INIT = "$GEN_PKG/StartupLoaderInit"
        private const val CLASS_STARTUP_CORE = "$PKG/StartupCore"
        private const val METHOD_INIT = "init"
        private const val METHOD_REGISTER = "register"

        private const val CLASS_STARTUP_TASK = "$PKG/StartupTask"
        private const val CLASS_STARTUP_TASK_REGISTER = "$PKG/StartupTaskRegister"
        private const val CLASS_BLOCK_EXECUTOR_FACTORY = "$PKG/executor/BlockExecutor\$Factory"
    }

    /**
     * incremental build support
     */
    // link RelativePath to StartupTaskRegisterInfo
    private val targetInfoMap = ConcurrentHashMap<String, StartupTaskRegisterInfo>()
    private lateinit var cacheInfoMapFile: File

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun getContext(
        project: Project?,
        android: AppExtension?,
        extension: StartupExtension?
    ): StartupContext = StartupContext(project, android, extension)

    override fun init(transformer: TransformEngine) {
        super.init(transformer)

        cacheInfoMapFile = File(context.buildDir(), "cacheInfoMap.json")

        // if isIncremental
        // fetch info map cache
        if (transformer.context.isIncremental) {
            if (cacheInfoMapFile.exists() && cacheInfoMapFile.isFile) {
                val jsonData = String(cacheInfoMapFile.inputStream().readBytes())
                val cacheInfoMap = gson.fromJson<Map<String, StartupTaskRegisterInfo>>(
                    jsonData,
                    object : TypeToken<Map<String, StartupTaskRegisterInfo>>() {}.type
                )
                context.logger.i("fetch cache info map: ${gson.toJson(cacheInfoMap)}")
                targetInfoMap.putAll(cacheInfoMap)
            } else {
                // fallback
                transformer.context.requestNotIncremental()
                context.logger.i("cache info map not exists, now requestNotIncremental".also {
                    println(it)
                })
            }
        }
    }

    /**
     * when incremental build
     * removed status go here only
     */
    override fun traverseIncremental(fileData: FileData, chain: ClassVisitorChain?) {
        super.traverseIncremental(fileData, chain)

        // remove from info map cache if file is matched
        if (fileData.status == Status.REMOVED) {
            targetInfoMap.remove(fileData.relativePath)?.let {
                context.logger.i("remove node ${it.nodeName}")
            }
        }
    }

    override fun traverse(relativePath: String, node: ClassNode) {
        super.traverse(relativePath, node)

        // filter classes if use @StartupTaskRegister
        node.visibleAnnotations?.find {
            it.desc == "L$CLASS_STARTUP_TASK_REGISTER;"
        }?.runCatching {
            context.logger.i("traverse $relativePath ${node.name}")
            val map = mutableMapOf<String, Any>()
            for (i in 0 until values.size step 2) {
                map[values[i] as String] = values[i + 1]
            }
            val info = StartupTaskRegisterInfo(node.name, map["id"] as String)
            map["idDependencies"]?.let {
                info.idDependencies = it as List<String>
            }
            map["executorFactory"]?.let {
                info.async = "L$CLASS_BLOCK_EXECUTOR_FACTORY;" != it
            }
            map["blockWhenAsync"]?.let {
                info.blockWhenAsync = it as Boolean
            }
            map["process"]?.let {
                info.process = it as String
            }
            map["priority"]?.let {
                info.priority = it as Int
            }
            targetInfoMap[relativePath] = info
        }
    }

    override fun afterTransform(engine: TransformEngine) {
        super.afterTransform(engine)

        val targetInfoList = targetInfoMap.values.filter {
            // check target class if is a StartupTask
            context.classGraph.instanceofClass(it.nodeName, CLASS_STARTUP_TASK)
        }.filter {
            val exclude = extension.getExcludeTaskList().contains(it.nodeName)
            if (exclude) {
                context.logger.i("exclude node ${it.nodeName}")
            }
            exclude.not()
        }
        if (targetInfoList.isEmpty()) {
            return
        }
        context.logger.i("found target info list -> \n${gson.toJson(targetInfoList)}")

        // sort and check
        val sort = StartupSort()
        val allProcess = targetInfoList.map { it.process }.toSet()
        allProcess.forEach { process ->
            val processName = if (process.isEmpty()) "main" else process
            val list = targetInfoList.filter { it.process == process }
            sort.sort(processName, list)
        }
        context.logger.i(buildString {
            append("startup dispatch order info at below:")
            appendLine().appendLine()
            append(sort.generateOrder())
        })
        context.logger.i(buildString {
            append("startup relationship info at below:")
            appendLine().appendLine()
            append(sort.generateRelationship())
        })
        context.logger.i(buildString {
            append("graphviz dot code at below:")
            appendLine().appendLine()
            append(sort.generateGraphviz())
            appendLine()
            append("copy dot code and paste in http://magjac.com/graphviz-visual-editor/")
        })

        // generate loader class
        val targetClasses = targetInfoList.map { it.nodeName }
        generateStartupLoaderInitClass(targetClasses)
    }

    private fun generateStartupLoaderInitClass(targetClasses: List<String>) {
        runCatching {
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
                "(L$CLASS_STARTUP_CORE;)V",
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
                    "(L$CLASS_STARTUP_TASK;)V",
                    false
                )
            }
            val labelEnd = Label()
            mv.visitLabel(labelEnd)
            mv.visitLocalVariable(
                "var0",
                "L$CLASS_STARTUP_CORE;",
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
            val dest = File(outputDirPath, "$CLASS_STARTUP_LOADER_INIT.class")
            dest.touch()
            writer.toByteArray().redirect(dest)
            context.logger.i(("generated $CLASS_STARTUP_LOADER_INIT(${dest.length()})" +
                    " success[File]:${dest.absolutePath}").also {
                println(it)
            })
        }.run {
            if (isFailure) {
                context.logger.e("generate loader init class error: ${exceptionOrNull()}")
            }
        }
    }

    override fun afterExecute() {
        // safe info map cache
        cacheInfoMapFile.delete()
        PrintWriter(FileOutputStream(cacheInfoMapFile), true).run {
            print(gson.toJson(targetInfoMap))
            flush()
            close()
            context.logger.i(("save info map cache(${cacheInfoMapFile.length()})" +
                    " success[File]:${cacheInfoMapFile.absolutePath}").also {
                kotlin.io.println(it)
            })
        }

        super.afterExecute()
    }

    override fun transformConfiguration(): TransformConfiguration {
        return object : TransformConfiguration {
            override fun consumesFeatureJars(): Boolean = extension.isConsumesFeatureJars()
        }
    }
}