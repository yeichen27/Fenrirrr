// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.gms.services) apply false
}

tasks.register("clean").configure {
    delete(rootProject.layout.buildDirectory.asFile)
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.compilerArgs.addAll(listOf("-Xmaxwarns", "1000", "-Xmaxerrs", "1000"))
    //options.compilerArgs.add("-Xlint:deprecation")
}