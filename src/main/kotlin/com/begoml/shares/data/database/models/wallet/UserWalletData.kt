package com.begoml.shares.data.database.models.wallet

import com.begoml.shares.data.database.models.user.UserData
import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.repositories.wallet.WalletStatus
import com.begoml.shares.tools.AppCurrency
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal
import java.time.LocalDateTime

internal object UserWalletData : Table() {
    val walletId: Column<String> = varchar("walletId", 100)

    val userId: Column<String> = reference("userId", UserData.userId).uniqueIndex()
    val name: Column<String> = varchar("username", 100)

    private val currency: Column<String> = varchar("currency", 10).default(AppCurrency.USD)
    val balances: Column<BigDecimal> = decimal("balances", 20, 8).default(BigDecimal(0))

    val status: Column<String> = varchar("status", 20).default("ACTIVE")

    // clientDefault is evaluated per insert; default(LocalDateTime.now()) was evaluated once at class load.
    private val createdAt: Column<LocalDateTime> = datetime("createdAt").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(walletId)

    fun toDomain(row: ResultRow): UserWallet {
        return UserWallet(
            walletId = row[walletId],
            userId = row[userId],
            name = row[name],
            balances = row[balances],
            currency = row[currency],
            status = WalletStatus.valueOf(row[status]),
            createdAt = row[createdAt],
        )
    }
}