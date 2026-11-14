package com.begoml.shares.data.repository.user

import com.begoml.shares.data.database.DatabaseProvider
import com.begoml.shares.data.database.models.user.UserData
import com.begoml.shares.data.database.models.user.UserProfileData
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.UserProfile
import com.begoml.shares.logic.services.user.models.params.UserUpdates
import com.begoml.shares.tools.parallelMap
import org.jetbrains.exposed.sql.insert
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

        // Each shard answers independently; the user lives on at most one of them.
        return results.firstNotNullOfOrNull { it }
    }

    override suspend fun updateUser(userId: String, updates: UserUpdates) {
        transaction(databaseProvider.getDatabase(userId)) {
            val updated = UserProfileData.update({ UserProfileData.userId eq userId }) { row ->
                if (updates.name != null) {
                    row[username] = updates.name
                }
                if (updates.age != null) {
                    row[age] = updates.age
                }
            }

            // Registration does not create a profile row, so the first update has to insert it.
            if (updated == 0) {
                UserProfileData.insert { row ->
                    row[UserProfileData.userId] = userId
                    row[username] = updates.name.orEmpty()
                    row[age] = updates.age
                }
            }
        }
    }

    override suspend fun updatePassword(userId: String, password: String) {
        transaction(databaseProvider.getDatabase(userId)) {
            UserData.update({ UserData.userId eq userId }) { row ->
                row[UserData.password] = password
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