plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    api("org.apache.logging.log4j:log4j-api:2.20.0")
    api("org.apache.logging.log4j:log4j-core:2.20.0")
}
