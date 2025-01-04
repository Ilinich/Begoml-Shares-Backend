package com.begoml.shares.logic.services.wallet

import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.repositories.wallet.UserWalletRepository

class UserWalletService(
    private val walletRepository: UserWalletRepository,
) {

    suspend fun getWallet(userId: String): UserWallet {
        return checkNotNull(walletRepository.findWalletByUserId(userId))
    }

    suspend fun findWallet(userId: String): UserWallet? {
        return walletRepository.findWalletByUserId(userId)
    }

    suspend fun createWallet(userId: String) {
        return walletRepository.createWallet(userId)
    }
}