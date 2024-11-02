package com.begoml.shares.data.repository.user

import com.begoml.shares.data.database.UserData
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.params.UserUpdates
import com.begoml.shares.tools.EMPTY_STRING
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImpl : UserRepository {

    init {
        transaction {
            SchemaUtils.create(UserData)
        }
    }

    override suspend fun findByEmail(email: String) = transaction {
        UserData.select { UserData.email eq email }
            .map(UserData::toDomain).firstOrNull()
    }

    override suspend fun getUsers() = transaction {
        UserData.selectAll().map(UserData::toDomain)
    }

    override suspend fun updateUser(email: String, updates: UserUpdates) {
        transaction {
            UserData.update({ UserData.email eq email }) { row ->
                row[UserData.email] = email
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
        val user = transaction {
            val userId = UserData.insertAndGetId { row ->
                row[UserData.email] = email
                row[username] = EMPTY_STRING
                row[UserData.password] = password
            }.value

            UserData.select { UserData.id eq userId }
                .map(UserData::toDomain)
                .firstOrNull()
        }
        return requireNotNull(user)
    }
}