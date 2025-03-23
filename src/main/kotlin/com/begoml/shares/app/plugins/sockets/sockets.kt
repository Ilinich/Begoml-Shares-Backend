package com.begoml.shares.app.plugins.sockets

import com.begoml.shares.presentation.exceptions.logger
import com.begoml.shares.presentation.features.events.SharePriceNotifier
import com.begoml.shares.presentation.features.events.WebSocketManager
import com.begoml.shares.presentation.tools.UserPrincipal
import com.begoml.shares.presentation.tools.safeUser
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.time.Duration

fun Application.configureSockets() {
    val json by inject<Json>()

    val webSocketManager by inject<WebSocketManager>()
    val sharePriceNotifier by inject<SharePriceNotifier>()
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(json)
        pingPeriod = Duration.ofSeconds(300)
        timeout = Duration.ofSeconds(300)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {

        authenticate {
            webSocket("/events") {
                val user = call.authentication.principal<UserPrincipal>()
                if (user == null) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
                    return@webSocket
                }

                webSocketManager.addSession(userId = user.userId, this)

                try {
                    for (frame in incoming) {
                        if (frame is Frame.Close) {
                            break
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e.message, e)
                } finally {
                    webSocketManager.removeSession(user.userId)
                }
            }
        }
    }

    sharePriceNotifier.startListening()
}