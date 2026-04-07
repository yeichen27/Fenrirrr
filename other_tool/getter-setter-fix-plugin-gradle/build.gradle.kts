import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.13.1"
}

group = "dev.ragnarok"
version = "1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        jetbrainsRuntime()
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.3.3") {
            useInstaller = false
            useCache = false
        }
        pluginVerifier()
        zipSigner()
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
