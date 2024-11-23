package com.begoml.shares.data.repository.wallet

import com.begoml.shares.data.database.DatabaseProvider
import com.begoml.shares.data.database.models.wallet.UserWalletData
import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.repositories.wallet.UserWalletRepository
import com.begoml.shares.logic.repositories.wallet.WalletStatus
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.util.*

class UserWalletRepositoryImpl(
    private val databaseProvider: DatabaseProvider,
) : UserWalletRepository {

    override suspend fun findWalletByUserId(userId: String): UserWallet? {
        return transaction(databaseProvider.getDatabase(userId)) {
            UserWalletData.select { UserWalletData.userId eq userId }
                .map(UserWalletData::toDomain).firstOrNull()
        }
    }

    override suspend fun createWallet(userId: String) {
        transaction(databaseProvider.getDatabase(userId)) {
            UserWalletData.insert { row ->
                row[walletId] = UUID.randomUUID().toString()
                row[UserWalletData.userId] = userId
            }
        }
    }

    override suspend fun updateName(userId: String, newWalletName: String) {
        transaction(databaseProvider.getDatabase(userId)) {
            UserWalletData.update({ UserWalletData.userId eq userId }) { row ->
                row[name] = newWalletName
            }
        }
    }

    override suspend fun updateStatus(userId: String, walletStatus: WalletStatus) {
        transaction(databaseProvider.getDatabase(userId)) {
            UserWalletData.update({ UserWalletData.userId eq userId }) { row ->
                row[status] = walletStatus.name
            }
        }
    }

    override suspend fun updateBalance(userId: String, newBalance: BigDecimal) {
        transaction(databaseProvider.getDatabase(userId)) {
            UserWalletData.update({ UserWalletData.walletId eq userId }) { row ->
                row[balances] = newBalance
            }
        }
    }
}