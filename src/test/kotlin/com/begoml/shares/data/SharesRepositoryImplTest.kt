package com.begoml.shares.data

import com.begoml.shares.data.repository.shares.SharesRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPooled
import kotlin.test.*

/** Needs a local Redis on 6379 (uses database 15); skipped when it is not running. */
class SharesRepositoryImplTest {

    private val redis = JedisPooled(
        HostAndPort("localhost", 6379),
        DefaultJedisClientConfig.builder().database(15).build(),
    )
    private val repository = SharesRepositoryImpl(
        json = Json { isLenient = true; ignoreUnknownKeys = true }, // same as the app
        databaseProvider = testDatabaseProvider(),
        redisClient = redis,
    )

    @BeforeEach
    fun setUp() {
        assumeTrue(runCatching { redis.ping() }.isSuccess, "Redis is not running")
        redis.flushDB()
    }

    @AfterEach
    fun tearDown() {
        runCatching { redis.flushDB() }
        redis.close()
    }

    @Test
    fun `prices are stored in one hash and refreshed for every share in the database`() = runBlocking {
        repository.updateShares()
        val symbols = repository.getShares().map { it.symbol }.toSet()
        assertTrue(symbols.isNotEmpty())

        assertEquals(setOf(SharesRepositoryImpl.PRICES_KEY), redis.keys("*"))
        assertEquals(symbols, repository.getPrices().keys)

        // Prices are refreshed even if the cache was lost: symbols come from the database.
        redis.flushDB()
        repository.updateSharePrices()

        assertEquals(symbols, repository.getPrices().keys)
        val symbol = symbols.first()
        assertEquals(repository.getPrices()[symbol], repository.getPrice(symbol))
    }
}
