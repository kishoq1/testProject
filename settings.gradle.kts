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
        maven (url  ="https://jitpack.io")
    }
}

// 1. Đặt tên dự án gốc trước
rootProject.name = "testProject"

// 2. Khai báo tất cả các module con sau đó
include(":app")
include(":extractor")
include(":shared")
include(":timeago-parser")