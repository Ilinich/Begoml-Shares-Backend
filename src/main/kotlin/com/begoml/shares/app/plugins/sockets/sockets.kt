package com.begoml.shares.app.plugins.sockets

import com.begoml.shares.presentation.services.RabbitConsumer
import com.begoml.shares.presentation.tools.RabbitEvents
import com.begoml.shares.tools.safeLaunch
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.time.Duration
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

fun Application.configureSockets() {
    val json by inject<Json>()
    val scope by inject<CoroutineScope>()

    val sessions = CopyOnWriteArrayList<WebSocketServerSession>() // TODO ?

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(json)
        pingPeriod = Duration.ofSeconds(300)
        timeout = Duration.ofSeconds(300)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {

        webSocket("/events") {
            sessions.add(this)

            try {
                send("Connected to price update service")

                for (frame in incoming) {
                    if (frame is Frame.Close) {
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                sessions.remove(this)
            }
        }
    }

    val rabbitConsumer by inject<RabbitConsumer>()
    scope.safeLaunch {
        rabbitConsumer.consumeMessages { message ->
            if (message == RabbitEvents.SharePricesUpdated) {
                message.hashCode()

               // broadcastPricesToWebSockets(sessions, updatedPrices)
            }
        }
    }
}

suspend fun broadcastPricesToWebSockets(sessions: List<WebSocketServerSession>, updatedPrices: List<String>) {
    val message = "Share prices updated: ${updatedPrices.joinToString(", ")}"
    sessions.forEach { session ->
        try {
            session.send(message)
        } catch (e: Exception) {
            println("Failed to send message to client: ${e.message}")
        }
    }
}