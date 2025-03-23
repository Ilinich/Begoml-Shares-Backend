package com.begoml.shares.presentation.features.events

import com.begoml.shares.presentation.exceptions.logger
import com.begoml.shares.presentation.features.shares.SharePriceResponse
import com.begoml.shares.tools.parallelMap
import com.begoml.shares.tools.safeLaunch
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class WebSocketManager(
    private val json: Json,
    private val scope: CoroutineScope,
) {

    private val sessions = ConcurrentHashMap<String, WebSocketServerSession>()

    fun addSession(userId: String, session: WebSocketServerSession) {
        sessions[userId] = session
    }

    fun removeSession(userId: String) {
        sessions.remove(userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun broadcastMessageNewSharesPrices(prices: Map<String, BigDecimal>) {
        scope.safeLaunch(
            handleException = { throwable ->
                logger.error(throwable.message, throwable)
            }
        ) {
            val newPrices = prices.map {
                SharePriceResponse(
                    symbol = it.key,
                    price = it.value,
                )
            }
            val message = json.encodeToString(newPrices)

            sessions.toList()
                .parallelMap(Dispatchers.IO.limitedParallelism(500)) { (userId, session) ->
                    if (session.isActive) {
                        try {
                            // TODO check whether this event should be sent to the user
                            session.send(message)
                        } catch (e: Exception) {
                            logger.error("Failed to send message to client: ${e.message}", e)
                        }
                    }
                }
        }
    }
}