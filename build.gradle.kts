plugins {
    // declare but don’t apply — subprojects can pick & choose
    kotlin("jvm")                   apply false
    kotlin("plugin.serialization")  apply false
    id("net.mamoe.mirai-console")   apply false
    id("io.freefair.lombok")        apply false
}

group   =   findProperty("group")   as String
version =   findProperty("version") as String

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
        "implementation"        ("com.google.code.gson:gson:2.13.1")
        "implementation"        ("org.json:json:20250107")
        "implementation"        ("org.apache.httpcomponents:httpclient:4.5.13")
        "api"                   ("com.alibaba.fastjson2:fastjson2:2.0.57")
        "implementation"        ("org.projectlombok:lombok:1.18.38")
        "api"                   ("net.mamoe:mirai-logging-log4j2:2.16.0")
    }

    // target Java 11
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
}
