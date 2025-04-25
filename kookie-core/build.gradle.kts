plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api("org.slf4j:slf4j-api:2.0.17")
    api("com.fasterxml.jackson.core:jackson-databind:2.18.3")
}
