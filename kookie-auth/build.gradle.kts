plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(path = ":kookie-core", configuration = "default"))
    implementation(project(path = ":kookie-data", configuration = "default"))


    implementation("com.google.guava:guava:33.4.8-jre")
}
