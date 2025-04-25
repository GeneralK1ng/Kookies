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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}