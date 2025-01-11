package com.begoml.shares.data.repository.shares

import com.begoml.shares.data.database.DatabaseProvider
import com.begoml.shares.data.database.models.shares.ShareData
import com.begoml.shares.data.remote.ShareDataRemote
import com.begoml.shares.logic.repositories.shares.Share
import com.begoml.shares.logic.repositories.shares.SharesRepository
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import redis.clients.jedis.Jedis
import redis.clients.jedis.params.ScanParams
import java.io.FileNotFoundException
import java.math.BigDecimal
import kotlin.random.Random

class SharesRepositoryImpl(
    private val json: Json,
    private val databaseProvider: DatabaseProvider,
    private val redisClient: Jedis,
) : SharesRepository {

    companion object {
        private const val PRICE_KEY = "share_price_symbol_"
    }

    override suspend fun getShares(): List<Share> {
        return transaction(databaseProvider.getShares()) {
            ShareData.selectAll().map(ShareData::toDomain)
        }
    }

    override suspend fun hasShares(): Boolean {
        return transaction(databaseProvider.getShares()) {
            ShareData.selectAll().limit(1).count() > 0
        }
    }

    override fun getPrices(): Map<String, BigDecimal> {
        val keys = redisClient.keys("$PRICE_KEY*")
        val prices = mutableMapOf<String, BigDecimal>()

        keys.forEach { key ->
            val price = redisClient.get(key)?.toBigDecimalOrNull()
            if (price != null) {
                prices[key.removePrefix(PRICE_KEY)] = price
            }
        }

        return prices
    }

    override suspend fun updateSharePrices() {
        val cursor = "0"
        val scanParams = ScanParams().match("$PRICE_KEY*")
        val priceEntries = mutableListOf<Pair<String, String>>()

        var currentCursor = cursor
        do {
            val scanResult = redisClient.scan(currentCursor, scanParams)

            val symbols = scanResult.result.map { it.removePrefix(PRICE_KEY) }
            val prices = loadPrices(symbols.toSet())

            prices.forEach { (symbol, price) ->
                priceEntries.add("$PRICE_KEY$symbol" to price.toString())
            }

            currentCursor = scanResult.cursor
        } while (currentCursor != "0")

        if (priceEntries.isNotEmpty()) {
            redisClient.mset(*priceEntries.flatMap { listOf(it.first, it.second) }.toTypedArray())
        }
    }

    private fun loadPrices(symbols: Set<String>): List<Pair<String, BigDecimal>> {
        return symbols.map {
            it to Random.nextDouble(0.05, 999.0).toBigDecimal()
        }
    }

    override suspend fun updateShares() {
        val shares = loadSharesFromJsonFile()

        transaction(databaseProvider.getShares()) {
            shares.forEach { share ->
                val symbolValue = share.symbol
                val shareIdValue = share.shareId
                if (shareIdValue != null && symbolValue != null) {
                    ShareData.insertIgnore {
                        it[shareId] = shareIdValue
                        it[symbol] = symbolValue
                        it[name] = share.name.orEmpty()
                        it[description] = share.description.orEmpty()
                        it[logoUrl] = share.logoUrl.orEmpty()
                        it[imageUrl] = share.imageUrl.orEmpty()
                    }

                    redisClient.set("$PRICE_KEY$symbolValue", share.price.toString())
                }
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