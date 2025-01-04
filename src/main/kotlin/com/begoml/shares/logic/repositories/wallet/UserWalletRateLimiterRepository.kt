package com.begoml.shares.logic.repositories.wallet

interface UserWalletRateLimiterRepository {

    suspend fun checkRateLimit(userId: String)
}