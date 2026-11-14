package com.begoml.shares.logic.repositories.shares

import java.math.BigDecimal

interface SharesRepository {

    suspend fun getShares(): List<Share>

    suspend fun getShare(symbol: String): Share?

    suspend fun updateShares()

    suspend fun updateSharePrices()

    suspend fun hasShares(): Boolean

    suspend fun getPrices(): Map<String, BigDecimal>

    suspend fun getPrice(symbol: String): BigDecimal?
}
