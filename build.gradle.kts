plugins {
    // declare but don’t apply — subprojects can pick & choose
    kotlin("jvm")         apply false
    kotlin("plugin.serialization") apply false
    id("net.mamoe.mirai-console") apply false
    id("io.freefair.lombok")      apply false
}

group = "org.kookies"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
        if (System.getenv("CI")?.toBoolean() != true) {
            maven("https://maven.aliyun.com/repository/public")
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.freefair.lombok")

    // common dependencies
    dependencies {
        "implementation"("com.google.code.gson:gson:2.10")
        "implementation"("org.json:json:20220924")
        "implementation"("org.apache.httpcomponents:httpclient:4.5.13")
        "api"          ("com.alibaba:fastjson:1.2.83")
        "implementation"("org.projectlombok:lombok:1.18.32")
    }

    // target Java 17
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
