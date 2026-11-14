package com.begoml.shares.logic.repositories.wallet

import java.math.BigDecimal

interface UserWalletRepository {

    suspend fun findWalletByUserId(userId: String): UserWallet?

    suspend fun createWallet(userId: String, name: String)

    suspend fun updateName(userId: String, newWalletName: String)

    suspend fun updateStatus(userId: String, walletStatus: WalletStatus)

    /**
     * Atomically adds [amount] to an ACTIVE wallet.
     * @return `true` if the balance was changed.
     */
    suspend fun deposit(userId: String, amount: BigDecimal): Boolean

    /**
     * Atomically subtracts [amount] from an ACTIVE wallet whose balance is at least [amount].
     * @return `true` if the balance was changed.
     */
    suspend fun withdraw(userId: String, amount: BigDecimal): Boolean
}
