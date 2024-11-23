package com.begoml.shares.data.repository.user

import com.begoml.shares.data.database.DatabaseProvider
import com.begoml.shares.data.database.models.user.UserData
import com.begoml.shares.data.database.models.user.UserProfileData
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.UserProfile
import com.begoml.shares.logic.services.user.models.params.UserUpdates
import com.begoml.shares.tools.parallelMap
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

class UserRepositoryImpl(
    private val databaseProvider: DatabaseProvider,
) : UserRepository {

    override suspend fun findByUserId(userId: String): User? {
        return transaction(databaseProvider.getDatabase(userId)) {
            UserData.select { UserData.userId eq userId }
                .map(UserData::toDomain).firstOrNull()
        }
    }

    override suspend fun findByUserProfileId(userId: String): UserProfile? {
        return transaction(databaseProvider.getDatabase(userId)) {
            UserProfileData.select { UserProfileData.userId eq userId }
                .map(UserProfileData::toDomain).firstOrNull()
        }
    }

    override suspend fun findUserByEmail(email: String): User? {
        val shards = databaseProvider.getShards()

        val results = shards.parallelMap { shard ->
            transaction(shard) {
                UserData.select { UserData.email eq email }
                    .map(UserData::toDomain)
                    .firstOrNull()
            }
        }

        return results.firstOrNull()
    }

    override suspend fun updateUser(userId: String, updates: UserUpdates) {
        transaction(databaseProvider.getDatabase(userId)) {
            UserProfileData.update({ UserProfileData.userId eq userId }) { row ->
                if (updates.name != null) {
                    row[username] = updates.name
                }
                if (updates.age != null) {
                    row[age] = updates.age
                }
            }
        }
    }

    override suspend fun createUser(email: String, password: String): User {
        val userId = generateUserId()
        val user = transaction(databaseProvider.getDatabase(userId)) {
            UserData.insertAndGetId { row ->
                row[UserData.userId] = userId
                row[UserData.email] = email
                row[UserData.password] = password
            }

            UserData.select { UserData.userId eq userId }
                .map(UserData::toDomain)
                .firstOrNull()
        }
        return requireNotNull(user)
    }

    private fun generateUserId(): String {
        return UUID.randomUUID().toString()
    }
}