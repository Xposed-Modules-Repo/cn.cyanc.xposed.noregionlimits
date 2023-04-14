pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        // ❗若你的 Plugin 版本过低，作为 Xposed 模块使用务必添加，其它情况可选
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        // ❗作为 Xposed 模块使用务必添加，其它情况可选
        maven("https://api.xposed.info/")
        // MavenCentral 有 2 小时缓存，若无法集成最新版本请添加此地址
//        maven("https://s01.oss.sonatype.org/content/repositories/releases")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // ❗若你的 Plugin 版本过低，作为 Xposed 模块使用务必添加，其它情况可选
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        // ❗作为 Xposed 模块使用务必添加，其它情况可选
        maven("https://api.xposed.info/")
        // MavenCentral 有 2 小时缓存，若无法集成最新版本请添加此地址
        maven("https://s01.oss.sonatype.org/content/repositories/releases")

        maven("https://jitpack.io")
    }
}

rootProject.name = "Not From China"
include(":app")
