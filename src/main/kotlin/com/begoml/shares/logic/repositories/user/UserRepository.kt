package com.begoml.shares.logic.repositories.user

import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.params.UserUpdates

interface UserRepository {

    suspend fun findByEmail(email: String): User?

    suspend fun createUser(email: String, password: String): User

    suspend fun getUsers(): List<User>

    suspend fun updateUser(
        email: String,
        updates: UserUpdates,
    )
}