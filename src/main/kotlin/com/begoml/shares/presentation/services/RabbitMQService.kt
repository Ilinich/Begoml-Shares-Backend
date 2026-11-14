package com.begoml.shares.presentation.services

import com.begoml.shares.presentation.tools.RabbitEvents
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogger

/**
 * Broadcasts application events to every running instance.
 *
 * Events go to a fanout exchange. Each instance consumes from its own server-named,
 * exclusive, auto-delete queue bound to that exchange, so every instance receives every
 * event (one shared queue would split events between instances, and each instance would
 * notify only its own WebSocket clients about some of them).
 */
class RabbitMQService(
    private val json: Json,
    private val logger: KLogger,
    host: String,
    port: Int,
) : RabbitEmitter, RabbitConsumer {

    companion object {
        private const val EXCHANGE = "begomlShares.events"
    }

    private val connectionFactory = ConnectionFactory().apply {
        this.host = host
        this.port = port
        isAutomaticRecoveryEnabled = true
    }

    private val events = Channel<RabbitEvents>(Channel.UNLIMITED)

    private lateinit var connection: Connection

    // RabbitMQ channels must not be shared between threads, so publishing and consuming use their own.
    private lateinit var publishChannel: com.rabbitmq.client.Channel
    private lateinit var consumeChannel: com.rabbitmq.client.Channel
    private val publishLock = Any()

    fun start() {
        connection = connectionFactory.newConnection()

        publishChannel = connection.createChannel()
        publishChannel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.FANOUT, true)

        consumeChannel = connection.createChannel()
        val queue = consumeChannel.queueDeclare().queue
        consumeChannel.queueBind(queue, EXCHANGE, "")

        val deliverCallback = DeliverCallback { _, delivery ->
            runCatching { json.decodeFromString<RabbitEvents>(String(delivery.body)) }
                .onSuccess { events.trySend(it) }
                .onFailure { logger.error("Cannot decode RabbitMQ event", it) }
        }
        consumeChannel.basicConsume(queue, true, deliverCallback, CancelCallback {})
    }

    fun stop() {
        events.close()
        runCatching { connection.close() }
            .onFailure { logger.warn("Failed to close RabbitMQ connection", it) }
    }

    override fun emit(message: RabbitEvents) {
        val body = json.encodeToString(message).toByteArray()
        synchronized(publishLock) {
            publishChannel.basicPublish(EXCHANGE, "", null, body)
        }
        logger.debug { "Sent: $message" }
    }

    override suspend fun consumeMessages(onMessage: suspend (RabbitEvents) -> Unit) {
        for (message in events) {
            onMessage(message)
        }
    }
}

interface RabbitEmitter {
    fun emit(message: RabbitEvents)
}

interface RabbitConsumer {
    suspend fun consumeMessages(onMessage: suspend (RabbitEvents) -> Unit)
}
