@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //maven(url = "https://artifactory-external.vkpartner.ru/artifactory/vk-id-captcha/android/")
        //maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

//https://github.com/esensar/kotlinx-serialization-msgpack
//apksigner verify --print-certs *.apk

rootProject.name = "Fenrir VK"
include(
    ":app_fenrir",
    ":app_filegallery",
    ":fenrir_common",
    ":picasso3",
    ":firebase-installations",
    ":image",
    ":material",
    ":preference",
    ":recyclerview",
    ":camera2"
)

//include(":native")
