import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("org.jetbrains.intellij") version "0.6.1"
    java
    kotlin("jvm") version "1.4.10"
}

val pom = loadPom()
group = pom.first
version = pom.second

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}
listOf("compileKotlin", "compileTestKotlin").forEach {
    tasks.getByName<KotlinCompile>(it) {
        kotlinOptions.jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.1"
    setPlugins(
        "java",
        "org.jetbrains.kotlin"
    )
    pluginName = "Startup-Navigator"
    updateSinceUntilBuild = false
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes(
        """
      <h2>1.0.0-beta01</h2>
      StartupTask之间导航，向上向下跳转<br>
      支持Kotlin<br>
      """
    )
}

fun loadPom(): Pair<String, String> {
    val properties = Properties()
    runCatching {
        properties.load(file("../gradle.properties").inputStream())
    }
    val group = properties.getProperty("POM_GROUP_ID", "com.zyhang.startup")
    val version = properties.getProperty("POM_PUBLISH_VERSION", "1.0-SNAPSHOT")
    println("pom group -> $group")
    println("pom version -> $version")
    return group to version
}