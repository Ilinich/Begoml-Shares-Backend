package com.begoml.shares.logic.services.wallet

import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.repositories.wallet.UserWalletRateLimiterRepository
import com.begoml.shares.logic.repositories.wallet.UserWalletRepository
import java.math.BigDecimal

class UserWalletService(
    private val walletRepository: UserWalletRepository,
    private val rateLimiter: UserWalletRateLimiterRepository,
) {

    suspend fun getWallet(userId: String): UserWallet {
        return checkNotNull(walletRepository.findWalletByUserId(userId))
    }

    suspend fun findWallet(userId: String): UserWallet? {
        return walletRepository.findWalletByUserId(userId)
    }

    suspend fun createWallet(userId: String, name: String) {
        return walletRepository.createWallet(userId = userId, name = name)
    }

    suspend fun updateName(userId: String, newWalletName: String) {
        return walletRepository.updateName(userId = userId, newWalletName = newWalletName)
    }

    suspend fun addBalance(userId: String, newBalance: BigDecimal) {
        return walletRepository.addBalance(userId = userId, newBalanceValue = newBalance)
    }

    suspend fun checkRateLimit(userId: String) = rateLimiter.checkRateLimit(userId)
}