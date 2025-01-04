package com.begoml.shares.data.repository.wallet

import com.begoml.shares.logic.exceptions.RateLimitException
import com.begoml.shares.logic.repositories.wallet.UserWalletRateLimiterRepository
import redis.clients.jedis.Jedis

class UserWalletRateLimiterRepositoryImpl(
    private val redisClient: Jedis,
) : UserWalletRateLimiterRepository {

    override suspend fun checkRateLimit(userId: String) {
        val key = "user_wallet_rate_limit:$userId"

        val currentCount = redisClient.get(key)?.toIntOrNull() ?: 0

        if (currentCount >= 10) {
            throw RateLimitException("Too many requests")
        }

        redisClient.incr(key)
        redisClient.expire(key, 60)
    }
}