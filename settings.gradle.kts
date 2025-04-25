pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // 阿里云国内代理仓库（CI 之外开启）
        if (System.getenv("CI")?.toBoolean() != true) {
            maven("https://maven.aliyun.com/repository/public")
        }
    }
    plugins {
        kotlin("jvm") version "1.8.0"
        kotlin("plugin.serialization") version "1.8.0"
        id("net.mamoe.mirai-console") version "2.16.0"
        id("io.freefair.lombok") version "8.6"
    }
}

rootProject.name = "Kookies"

include(":kookie-core")
include(":kookie-plugin")
include(":kookie-auth")
include(":kookie-data")