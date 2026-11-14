package com.begoml.shares.logic.services.wallet

import com.begoml.shares.logic.exceptions.CommonException
import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.repositories.wallet.UserWalletRateLimiterRepository
import com.begoml.shares.logic.repositories.wallet.UserWalletRepository
import com.begoml.shares.logic.repositories.wallet.WalletStatus
import io.ktor.http.*
import java.math.BigDecimal

class UserWalletService(
    private val walletRepository: UserWalletRepository,
    private val rateLimiter: UserWalletRateLimiterRepository,
) {

    companion object {
        // Matches the scale of the `balances` column (decimal(20, 8)).
        private const val MAX_AMOUNT_SCALE = 8
    }

    suspend fun getWallet(userId: String): UserWallet {
        return walletRepository.findWalletByUserId(userId) ?: throw CommonException(
            status = HttpStatusCode.NotFound,
            message = "Wallet not found",
        )
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

    suspend fun deposit(userId: String, amount: BigDecimal) {
        validateAmount(amount)
        val wallet = getWallet(userId)
        ensureActive(wallet)

        if (!walletRepository.deposit(userId = userId, amount = amount)) {
            throw CommonException(HttpStatusCode.Conflict, "Deposit failed")
        }
    }

    suspend fun withdraw(userId: String, amount: BigDecimal) {
        validateAmount(amount)
        val wallet = getWallet(userId)
        ensureActive(wallet)

        if (!walletRepository.withdraw(userId = userId, amount = amount)) {
            throw CommonException(HttpStatusCode.Forbidden, "Insufficient balance")
        }
    }

    suspend fun checkRateLimit(userId: String) = rateLimiter.checkRateLimit(userId)

    private fun validateAmount(amount: BigDecimal) {
        if (amount.signum() <= 0) {
            throw CommonException(HttpStatusCode.BadRequest, "Amount must be positive")
        }
        if (amount.stripTrailingZeros().scale() > MAX_AMOUNT_SCALE) {
            throw CommonException(HttpStatusCode.BadRequest, "Amount has too many decimal places")
        }
    }

    private fun ensureActive(wallet: UserWallet) {
        if (wallet.status != WalletStatus.ACTIVE) {
            throw CommonException(HttpStatusCode.Forbidden, "Wallet is ${wallet.status.name.lowercase()}")
        }
    }
}
