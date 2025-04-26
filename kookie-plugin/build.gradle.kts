plugins {
    kotlin("jvm")
    id("net.mamoe.mirai-console")
}

dependencies {
    api(project(path = ":kookie-core", configuration = "default"))
    api(project(path = ":kookie-auth", configuration = "default"))
}

group = findProperty("group") as String
version = findProperty("version") as String

