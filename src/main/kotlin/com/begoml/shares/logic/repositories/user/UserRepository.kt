package com.begoml.shares.logic.repositories.user

import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.UserProfile
import com.begoml.shares.logic.services.user.models.params.UserUpdates

interface UserRepository {

    suspend fun findUserByEmail(email: String): User?

    suspend fun findByUserId(userId: String): User?

    suspend fun findByUserProfileId(userId: String): UserProfile?

    suspend fun createUser(email: String, password: String): User

    suspend fun updatePassword(userId: String, password: String)

    /** Updates the user's profile, creating it if the user does not have one yet. */
    suspend fun updateUser(
        userId: String,
        updates: UserUpdates,
    )
}
