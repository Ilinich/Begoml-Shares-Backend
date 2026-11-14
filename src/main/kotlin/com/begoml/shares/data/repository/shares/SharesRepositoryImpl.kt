package com.begoml.shares.data.repository.shares

import com.begoml.shares.data.database.DatabaseProvider
import com.begoml.shares.data.database.dbQuery
import com.begoml.shares.data.database.models.shares.ShareData
import com.begoml.shares.data.remote.ShareDataRemote
import com.begoml.shares.logic.repositories.shares.Share
import com.begoml.shares.logic.repositories.shares.SharesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import redis.clients.jedis.UnifiedJedis
import java.io.FileNotFoundException
import java.math.BigDecimal
import kotlin.random.Random

class SharesRepositoryImpl(
    private val json: Json,
    private val databaseProvider: DatabaseProvider,
    private val redisClient: UnifiedJedis,
) : SharesRepository {

    companion object {
        // All prices live in one hash (symbol -> price): reading them is a single HGETALL
        // instead of KEYS, which scans the whole keyspace and blocks Redis.
        internal const val PRICES_KEY = "share_prices"
    }

    override suspend fun getShares(): List<Share> {
        return dbQuery(databaseProvider.getShares()) {
            ShareData.selectAll().map(ShareData::toDomain)
        }
    }

    override suspend fun getShare(symbol: String): Share? {
        return dbQuery(databaseProvider.getShares()) {
            ShareData.select { ShareData.symbol eq symbol }
                .map(ShareData::toDomain).firstOrNull()
        }
    }

    override suspend fun hasShares(): Boolean {
        return dbQuery(databaseProvider.getShares()) {
            ShareData.selectAll().limit(1).count() > 0
        }
    }

    override suspend fun getPrices(): Map<String, BigDecimal> = withContext(Dispatchers.IO) {
        redisClient.hgetAll(PRICES_KEY)
            .mapNotNull { (symbol, price) -> price.toBigDecimalOrNull()?.let { symbol to it } }
            .toMap()
    }

    override suspend fun getPrice(symbol: String): BigDecimal? = withContext(Dispatchers.IO) {
        redisClient.hget(PRICES_KEY, symbol)?.toBigDecimalOrNull()
    }

    override suspend fun updateSharePrices() {
        // Symbols come from the database, the source of truth, not from whatever is cached in Redis.
        val symbols = getShares().map { it.symbol }
        if (symbols.isEmpty()) return

        val prices = loadPrices(symbols).mapValues { (_, price) -> price.toPlainString() }
        withContext(Dispatchers.IO) {
            redisClient.hset(PRICES_KEY, prices)
        }
    }

    // TODO replace with a real market data source
    private fun loadPrices(symbols: List<String>): Map<String, BigDecimal> {
        return symbols.associateWith { Random.nextDouble(0.05, 999.0).toBigDecimal() }
    }

    override suspend fun updateShares() {
        val shares = loadSharesFromJsonFile()
            .filter { it.shareId != null && it.symbol != null }

        dbQuery(databaseProvider.getShares()) {
            shares.forEach { share ->
                ShareData.insertIgnore {
                    it[shareId] = share.shareId!!
                    it[symbol] = share.symbol!!
                    it[name] = share.name.orEmpty()
                    it[description] = share.description.orEmpty()
                    it[logoUrl] = share.logoUrl.orEmpty()
                    it[imageUrl] = share.imageUrl.orEmpty()
                }
            }
        }

        val prices = shares.associate { it.symbol!! to (it.price ?: BigDecimal.ZERO).toPlainString() }
        if (prices.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                redisClient.hset(PRICES_KEY, prices)
            }
        }
    }

    private fun loadSharesFromJsonFile(): List<ShareDataRemote> {
        val resourceStream = javaClass.classLoader.getResourceAsStream("json/shares.json")
            ?: throw FileNotFoundException("File not found in resources: json/shares.json")

        val fileText = resourceStream.bufferedReader().use { it.readText() }

        return json.decodeFromString<List<ShareDataRemote>>(fileText)
    }
}
