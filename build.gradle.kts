plugins {
    kotlin("jvm") version "1.9.10"
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "1.9.10"
}

group = "com.begoml.shares"
version = "0.1.0"

application {
    mainClass.set("com.begoml.shares.app.ApplicationKt")

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
    implementation("org.postgresql:postgresql:42.7.2")

    implementation("redis.clients:jedis:5.2.0")

    implementation("org.mindrot:jbcrypt:0.4")

    implementation(libs.koin.ktor)

    implementation("org.quartz-scheduler:quartz:2.5.0")

    implementation(libs.kotlin.logging)
    implementation("ch.qos.logback:logback-classic:1.5.13")
    implementation("org.slf4j:slf4j-api:1.7.32")

    implementation("com.rabbitmq:amqp-client:5.18.0")

    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:2.1.214")
}

tasks.test {
    useJUnitPlatform()
}
