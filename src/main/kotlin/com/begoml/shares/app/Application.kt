package com.begoml.shares.app

import com.begoml.shares.app.di.*
import com.begoml.shares.app.plugins.authentication.authentication
import com.begoml.shares.app.plugins.routing.configureRouting
import com.begoml.shares.app.plugins.serialization.configureSerialization
import com.begoml.shares.app.plugins.sockets.configureSockets
import com.begoml.shares.app.plugins.statuspages.statusPages
import com.begoml.shares.core.AppQuartzScheduler
import com.begoml.shares.presentation.services.RabbitMQService
import io.ktor.client.plugins.websocket.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import java.sql.DriverManager

val DATABASE_URL = "jdbc:postgresql://localhost:5432/begoml_share"
val DATABASE_URL_SECOND = "jdbc:postgresql://localhost:5432/begoml_shares_second"

val DATABASE_URL_SHARES = "jdbc:postgresql://localhost:5432/begoml_shares_shares"

val DATABASE_USER = "postgres"
val DATABASE_PASSWORD = "Postgres383"

fun main() {
    updatePostgresConfig(DATABASE_URL)
    updatePostgresConfig(DATABASE_URL_SECOND)
    updatePostgresConfig(DATABASE_URL_SHARES)

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

        modules(moduleFeatureServices)
        modules(moduleCore)

        modules(moduleAppWebSocket)
        modules(moduleAppEvents)

        modules(moduleServices)
        modules(moduleRepositories)
        modules(moduleDatabase)

        modules(moduleTools)
        modules(rabbitModule)
        modules(loggerModule)
    }

    authentication()
    configureRouting()
    statusPages()
    configureSerialization()

    startRabbitMQService()
    startServices()

    configureSockets()
}

fun Application.startServices() {
    val appQuartzSchedulerImpl by inject<AppQuartzScheduler>()
    appQuartzSchedulerImpl.start()
}

fun Application.startRabbitMQService() {
    val rabbitMQService by inject<RabbitMQService>()
    rabbitMQService.start()
}

/**
 * Updates PostgreSQL configuration to enable replication and allow remote connections.
 *
 * The method establishes a connection to the PostgreSQL server using the provided URL,
 * then executes a series of SQL commands to modify system parameters:
 *
 * - `max_wal_senders`: Sets the maximum number of replication sender processes.
 * - `wal_level`: Sets the level of write-ahead logging to support replication.
 * - `max_replication_slots`: Configures the maximum number of replication slots available.
 * - `listen_addresses`: Allows the server to accept connections from any network interface.
 *
 * Finally, it reloads the server configuration and ensures a replication role exists.
 *
 * @param url the JDBC URL to connect to the PostgreSQL database.
 *
 * @throws SQLException if an error occurs while executing SQL commands.
 * @throws IllegalArgumentException if the provided URL is invalid or null.
 */
fun updatePostgresConfig(url: String) {
    runCatching {
        DriverManager.getConnection(url, DATABASE_USER, DATABASE_PASSWORD).use { connection ->
            connection.createStatement().use { stmt ->

                stmt.execute("ALTER SYSTEM SET max_wal_senders = 3;")
                stmt.execute("ALTER SYSTEM SET wal_level = 'replica';")
                stmt.execute("ALTER SYSTEM SET max_replication_slots = 1;")
                stmt.execute("ALTER SYSTEM SET listen_addresses = '*';")

                stmt.execute("SELECT pg_reload_conf();")

                stmt.execute(
                    """
                    DO $$ BEGIN
                        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'replicator') THEN
                            CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'replicator_password';
                        END IF;
                    END $$;
                """.trimIndent()
                )
            }
        }
    }.onFailure { exception ->
        println("Error updating PostgreSQL configuration for $url: ${exception.message}")
        exception.printStackTrace()
    }
}