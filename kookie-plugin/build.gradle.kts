plugins {
    kotlin("jvm")
    id("net.mamoe.mirai-console")
}

dependencies {
    api(project(path = ":kookie-core", configuration = "default"))
}
