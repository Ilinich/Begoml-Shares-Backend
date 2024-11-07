plugins {
    kotlin("jvm") version "1.9.10"
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "1.9.10"
}

group = "com.begoml.shares"
version = "0.1.0"

application {
    mainClass.set(" com.begoml.shares.app.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(libs.bundles.ktor.server)

    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-cio-jvm")

    implementation(libs.ktor.swagger)

    implementation(libs.bundles.exposed)
    implementation("org.postgresql:postgresql:42.5.0")

    implementation(libs.koin.ktor)

    implementation(libs.kotlin.logging)
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}
