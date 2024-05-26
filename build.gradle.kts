plugins {
    val kotlinVersion = "1.8.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.16.0"
    id("io.freefair.lombok") version "8.6"
}

group = "org.kookies"
version = "0.1.3"

repositories {
    if (System.getenv("CI")?.toBoolean() != true) {
        maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    }
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.json:json:20220924")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    api("com.alibaba:fastjson:1.2.83")
    implementation("org.projectlombok:lombok:1.18.32")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // MyBatis dependencies
//    implementation("org.mybatis:mybatis:3.5.10")
//    implementation("mysql:mysql-connector-java:8.0.34") // 使用 MySQL 数据库
}
