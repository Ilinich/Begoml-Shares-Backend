package com.begoml.shares.app

import com.begoml.shares.app.di.*
import com.begoml.shares.app.plugins.authentication.authentication
import com.begoml.shares.app.plugins.routing.configureRouting
import com.begoml.shares.app.plugins.serialization.configureSerialization
import com.begoml.shares.app.plugins.statuspages.statusPages
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.plugin.Koin
import java.sql.DriverManager

val DATABASE_URL = "jdbc:postgresql://localhost:5432/begomlshare"
val DATABASE_USER = "postgres"
val DATABASE_PASSWORD = "Postgres383"

fun main() {
    updatePostgresConfig()
    Database.connect(
        url = DATABASE_URL,
        driver = "org.postgresql.Driver",
        user = DATABASE_USER,
        password = DATABASE_PASSWORD,
    )

    embeddedServer(
        factory = CIO,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {

    install(Koin) {
        modules(moduleControllers)
        modules(moduleServices)
        modules(moduleRepositories)
        modules(moduleTools)
        modules(moduleDatabase)
    }

    authentication()
    configureRouting()
    statusPages()
    configureSerialization()
}

fun updatePostgresConfig() {
    runCatching {
        DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD).use { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute("ALTER SYSTEM SET max_wal_senders = 3;")
                stmt.execute("ALTER SYSTEM SET wal_level = 'replica';")
                stmt.execute("SELECT pg_reload_conf();")
            }
            println("PostgreSQL configuration updated successfully.")
        }
    }.onFailure { exception ->
        println("Error updating PostgreSQL configuration: ${exception.message}")
        exception.printStackTrace()
    }
}