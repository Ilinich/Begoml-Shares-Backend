package com.begoml.shares.presentation.services

import com.begoml.shares.presentation.tools.RabbitEvents
import com.begoml.shares.tools.safeLaunch
import com.rabbitmq.client.Connection
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RabbitMQService(
    val scope: CoroutineScope,
    val json: Json,
) : RabbitEmitter, RabbitConsumer {

    private val connectionFactory = ConnectionFactory().apply {
        host = "localhost"
        port = 5672
    }

    private val channel = Channel<RabbitEvents>(Channel.UNLIMITED)

    private lateinit var connection: Connection
    private lateinit var rabbitChannel: com.rabbitmq.client.Channel

    private var job: Job? = null

    fun start() {
        connection = connectionFactory.newConnection()
        rabbitChannel = connection.createChannel()
        rabbitChannel.queueDeclare("begomlShares", false, false, false, null)

        job?.cancel()
        job = scope.safeLaunch {
            val deliverCallback = DeliverCallback { _, delivery ->
                val message = String(delivery.body)
                val event = json.decodeFromString<RabbitEvents>(message)

                scope.safeLaunch {
                    channel.send(event)
                }
            }

            rabbitChannel.basicConsume("begomlShares", true, deliverCallback, CancelCallback {})
        }
    }

    fun stop() {
        job?.cancel()
        rabbitChannel.close()
        connection.close()
    }

    override fun emit(message: RabbitEvents) {
        val jsonMessage = json.encodeToString(message)
        rabbitChannel.basicPublish("", "begomlShares", null, jsonMessage.toByteArray())
        println("Sent: $message")
    }

    override fun consumeMessages(onMessage: (RabbitEvents) -> Unit) {
        scope.safeLaunch {
            for (message in channel) {
                onMessage(message)
            }
        }
    }
}

interface RabbitEmitter {
    fun emit(message: RabbitEvents)
}

interface RabbitConsumer {
    fun consumeMessages(onMessage: (RabbitEvents) -> Unit)
}