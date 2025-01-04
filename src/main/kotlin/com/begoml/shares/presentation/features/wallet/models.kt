package com.begoml.shares.presentation.features.wallet

import com.begoml.shares.tools.BigDecimalSerializer
import com.begoml.shares.tools.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class UserWalletRemoteResponse(
    val walletId: String,

    val name: String,

    val currency: String,

    @Serializable(with = BigDecimalSerializer::class)
    val balances: BigDecimal,

    val status: String,

    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
)


@Serializable
data class UserWalletNameRemoteParams(
    val name: String,
)

@Serializable
data class UserWalletWithdrawRemoteParams(
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val confirmationCode: String,
)

@Serializable
data class UserWalletDepositRemoteParams(
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val confirmationCode: String,
)