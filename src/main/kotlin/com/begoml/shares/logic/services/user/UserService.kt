package com.begoml.shares.logic.services.user

import com.begoml.shares.logic.exceptions.CommonException
import com.begoml.shares.logic.exceptions.ErrorKind
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.UserProfile
import com.begoml.shares.logic.services.user.models.params.UserUpdates

class UserService(
    private val userRepository: UserRepository,
) {
    suspend fun getUser(userId: String): User {
        return userRepository.findByUserId(userId = userId) ?: throw CommonException(
            kind = ErrorKind.BAD_REQUEST,
            message = "User not found"
        )
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return userRepository.findByUserProfileId(userId = userId)
    }

    suspend fun updateUser(
        userId: String,
        updates: UserUpdates,
    ) {
        validRegister(updates)

        userRepository.updateUser(
            userId = userId,
            updates = updates,
        )
    }

    private fun validRegister(update: UserUpdates) {
        require(update.name != null || update.age != null) { "Nothing to update." }

        update.name?.let { name ->
            require(name.isNotBlank() && name.length <= MAX_NAME_LENGTH) { "User name is invalid." }
        }

        update.age?.let { age ->
            require(age in MIN_AGE..MAX_AGE) { "User age is invalid." }
        }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 100 // UserProfileData.username is varchar(100)
        const val MIN_AGE = 0
        const val MAX_AGE = 150
    }
}
