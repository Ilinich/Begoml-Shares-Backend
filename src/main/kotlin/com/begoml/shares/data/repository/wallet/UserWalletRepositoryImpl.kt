package com.begoml.shares.data.repository.wallet

import com.begoml.shares.data.database.DatabaseProvider
import com.begoml.shares.data.database.models.wallet.UserWalletData
import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.repositories.wallet.UserWalletRepository
import com.begoml.shares.logic.repositories.wallet.WalletStatus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
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

    override suspend fun createWallet(userId: String, name: String) {
        transaction(databaseProvider.getDatabase(userId)) {
            UserWalletData.insert { row ->
                row[walletId] = UUID.randomUUID().toString()
                row[UserWalletData.name] = name
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

    // Single UPDATE ... SET balances = balances + ? so concurrent requests cannot lose updates.
    override suspend fun deposit(userId: String, amount: BigDecimal): Boolean {
        val updated = transaction(databaseProvider.getDatabase(userId)) {
            UserWalletData.update({
                (UserWalletData.userId eq userId) and
                    (UserWalletData.status eq WalletStatus.ACTIVE.name)
            }) { row ->
                row.update(UserWalletData.balances, UserWalletData.balances + amount)
            }
        }
        return updated > 0
    }

    // The balance check lives in the WHERE clause, so check and debit are one atomic statement.
    override suspend fun withdraw(userId: String, amount: BigDecimal): Boolean {
        val updated = transaction(databaseProvider.getDatabase(userId)) {
            UserWalletData.update({
                (UserWalletData.userId eq userId) and
                    (UserWalletData.status eq WalletStatus.ACTIVE.name) and
                    (UserWalletData.balances greaterEq amount)
            }) { row ->
                row.update(UserWalletData.balances, UserWalletData.balances - amount)
            }
        }
        return updated > 0
    }
}
