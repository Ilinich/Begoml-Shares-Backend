package com.begoml.shares.logic.repositories.wallet

import java.math.BigDecimal
import java.time.LocalDateTime

data class UserWallet(
    val walletId: String,

    val userId: String,

    val name: String,

    val currency: String,
    val balances: BigDecimal,

    val status: WalletStatus,

    val createdAt: LocalDateTime,
)

enum class WalletStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED
}