package com.begoml.shares.logic.repositories.user

import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.params.UserUpdates

interface UserRepository {

    suspend fun findByUserId(userId: String): User?

    suspend fun createUser(email: String, password: String): User

    suspend fun updateUser(
        userId: String,
        updates: UserUpdates,
    )
}