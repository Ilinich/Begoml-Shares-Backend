package com.begoml.shares.logic.services.user

import com.begoml.shares.logic.exceptions.CommonException
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.params.UserUpdates
import io.ktor.http.*

class UserService(
    private val userRepository: UserRepository,
) {
    suspend fun getUser(userId: String): User {
        return userRepository.findByUserId(userId = userId) ?: throw CommonException(
            status = HttpStatusCode.BadRequest,
            message = "User not found"
        )
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
        val age = update.age

        require(
            age != null
        ) { "User age is invalid." }

        require(
            !update.name.isNullOrBlank()
        ) { "User name is invalid." }
    }
}