package com.begoml.shares.data.repository.wallet

import com.begoml.shares.logic.exceptions.RateLimitException
import com.begoml.shares.logic.repositories.wallet.UserWalletRateLimiterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.params.SetParams

class UserWalletRateLimiterRepositoryImpl(
    private val redisClient: UnifiedJedis,
) : UserWalletRateLimiterRepository {

    override suspend fun checkRateLimit(userId: String) {
        val key = "user_wallet_rate_limit:$userId"

        // Create the window with its TTL only if it does not exist yet, then count atomically.
        // The window is fixed: later requests do not extend it.
        val count = withContext(Dispatchers.IO) {
            redisClient.set(key, "0", SetParams().nx().ex(WINDOW_SECONDS))
            redisClient.incr(key)
        }

        if (count > MAX_REQUESTS) {
            throw RateLimitException("Too many requests")
        }
    }

    private companion object {
        const val MAX_REQUESTS = 3L
        const val WINDOW_SECONDS = 30L
    }
}