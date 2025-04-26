pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/maven-public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        if (System.getenv("CI")?.toBoolean() != true) {
            maven("https://maven.aliyun.com/repository/public")
        }
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "1.9.24"
        kotlin("plugin.serialization") version "1.9.24"
        id("net.mamoe.mirai-console") version "2.16.0"
        id("io.freefair.lombok") version "8.13.1"
    }
}

rootProject.name = "Kookies"

include(":kookie-core")
include(":kookie-plugin")
include(":kookie-auth")
include(":kookie-data")