package com.begoml.shares.presentation

import com.begoml.shares.presentation.services.RabbitMQService
import com.begoml.shares.presentation.tools.RabbitEvents
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.net.Socket
import kotlin.test.*

/** Needs a local RabbitMQ on 5672; skipped when it is not running. */
class RabbitMQServiceTest {

    private fun newService() = RabbitMQService(
        json = Json,
        logger = KotlinLogging.logger {},
        host = "localhost",
        port = 5672,
    )

    @Test
    fun `every instance receives every event`() = runBlocking {
        assumeTrue(runCatching { Socket("localhost", 5672).close() }.isSuccess, "RabbitMQ is not running")

        val instances = List(2) { newService().apply { start() } }
        val received = instances.map { CompletableDeferred<RabbitEvents>() }
        val consumers = instances.zip(received).map { (instance, result) ->
            launch(Dispatchers.IO) { instance.consumeMessages { result.complete(it) } }
        }
        try {
            instances.first().emit(RabbitEvents.SharePricesUpdated)

            val events = withTimeout(5_000) { received.awaitAll() }
            events.forEach { assertEquals(RabbitEvents.SharePricesUpdated, it) }
        } finally {
            consumers.forEach { it.cancel() }
            instances.forEach { it.stop() }
        }
    }
}
