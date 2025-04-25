plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":kookie-core"))

    // JPA & Hibernate
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("org.hibernate:hibernate-core:6.6.13.Final")

    // Postgres SQL 驱动
    implementation("org.postgresql:postgresql:42.7.5")
}
