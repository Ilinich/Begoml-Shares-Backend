package com.begoml.shares.logic.repositories.wallet

import java.math.BigDecimal

interface UserWalletRepository {

    suspend fun findWalletByUserId(userId: String): UserWallet?

    suspend fun createWallet(userId: String, name: String)

    suspend fun updateName(userId: String, newWalletName: String)

    suspend fun updateStatus(userId: String, walletStatus: WalletStatus)

    suspend fun addBalance(userId: String, newBalanceValue: BigDecimal)
}