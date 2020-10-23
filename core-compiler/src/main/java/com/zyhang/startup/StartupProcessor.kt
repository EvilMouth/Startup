package com.zyhang.startup

import com.google.gson.Gson
import com.squareup.javapoet.*
import com.sun.tools.javac.code.Symbol.ClassSymbol
import com.zyhang.startup.executor.BlockExecutor
import com.zyhang.startup.model.STData
import com.zyhang.startup.model.STInfo
import com.zyhang.startup.utils.StartupConst
import java.io.IOException
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

class StartupProcessor : BaseProcessor() {
    override fun process(annotations: Set<TypeElement?>?, roundEnv: RoundEnvironment): Boolean {
        if (annotations == null || annotations.isEmpty()) return false
        for (element in roundEnv.getElementsAnnotatedWith(StartupTaskRegister::class.java)) {
            if (element !is ClassSymbol) {
                continue
            }

            // 检查是否继承StartupTask
            val startupTaskClsName = StartupTask::class.java.name
            val isStartupTask = isConcreteSubType(element, startupTaskClsName)
            if (!isStartupTask) {
                warn("$TAG, %s 未继承 %s，请检查", element.toString(), startupTaskClsName)
                continue
            }

            // 取得StartupTask信息
            val startupTaskRegister = element.getAnnotation(
                StartupTaskRegister::class.java
            )
            val id = startupTaskRegister.id
            val idDependencies = startupTaskRegister.idDependencies
            var executorFactory: TypeMirror? =
                null // https://stackoverflow.com/questions/59415133/annotation-processing-exception-failed-to-analyze-java-lang-reflect-invocation
            try {
                startupTaskRegister.executorFactory
            } catch (e: MirroredTypeException) {
                executorFactory = e.typeMirror
            }
            val blockWhenAsync = startupTaskRegister.blockWhenAsync
            val process = startupTaskRegister.process
            val clsName = element.className()
            val clsSimpleName = element.simpleName.toString()

            // build
            val typeSpec = TypeSpec.classBuilder(clsSimpleName + StartupConst.GEN_CLASS_SUFFIX)
                .addAnnotation(
                    AnnotationSpec.builder(STInfo::class.java)
                        .addMember(
                            "meta",
                            "\$S",
                            buildMeta(id, idDependencies, executorFactory, process)
                        )
                        .build()
                )
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(STData::class.java)
                .addField(StartupTask::class.java, "startupTask", Modifier.PRIVATE)
                .addMethod(
                    MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement(
                            "super(\$S, \$L, \$T.class, \$L, \$S)",
                            id,
                            buildIdDependencies(idDependencies),
                            executorFactory,
                            blockWhenAsync,
                            process
                        )
                        .build()
                )
                .addMethod(
                    MethodSpec.methodBuilder(StartupConst.METHOD_STARTUP)
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement(
                            "if (startupTask == null) startupTask = new \$T()",
                            typeName(clsName)
                        )
                        .addStatement("startupTask.\$L()", StartupConst.METHOD_STARTUP)
                        .build()
                )
                .build()
            try {
                JavaFile.builder(StartupConst.GEN_PKG, typeSpec)
                    .build()
                    .writeTo(filer)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(StartupTaskRegister::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }

    companion object {
        private const val TAG = "StartupProcessor"

        private fun buildMeta(
            id: String,
            idDependencies: Array<String>,
            executorFactory: TypeMirror?,
            processName: String
        ): String {
            val map: MutableMap<String, Any> = HashMap()
            map["id"] = id
            map["idDependencies"] = idDependencies
            map["async"] =
                BlockExecutor.Factory::class.java.canonicalName != executorFactory.toString()
            map["process"] = processName
            return Gson().toJson(map)
        }

        private fun buildIdDependencies(idDependencies: Array<String>): CodeBlock {
            val builder = CodeBlock.builder()
            if (listOf(*idDependencies).isEmpty()) {
                builder.add(
                    "new \$T{}",
                    Array<String>::class.java
                )
            } else {
                builder.add(
                    "new \$T{\$L}",
                    Array<String>::class.java,
                    "\"" + java.lang.String.join("\", \"", *idDependencies) + "\""
                )
            }
            return builder.build()
        }
    }
}