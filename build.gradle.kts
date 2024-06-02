plugins {
    val kotlinVersion = "1.8.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.16.0"
    id("io.freefair.lombok") version "8.6"
}

group = "com.generalk1ng.kookies"
version = "0.1.5"

repositories {
    if (System.getenv("CI")?.toBoolean() != true) {
        maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    }
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.json:json:20240303")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    api("com.alibaba:fastjson:1.2.83")
    implementation("org.projectlombok:lombok:1.18.32")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    api("net.mamoe:mirai-silk-converter:0.0.5")

    implementation("com.aliyun:openapiutil:0.2.1")
    implementation("com.aliyun:tea-openapi:0.3.1")
}
