package com.begoml.shares.logic.services.user

import com.begoml.shares.logic.exceptions.CommonException
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.logic.services.user.models.params.UserUpdates
import io.ktor.http.*

class UserService(
    private val userRepository: UserRepository,
) {
    suspend fun getUser(email: String): User {
        return userRepository.findByEmail(email) ?: throw CommonException(
            status = HttpStatusCode.BadRequest,
            message = "User not found"
        )
    }

    suspend fun updateUser(
        email: String,
        updates: UserUpdates,
    ) {
        validRegister(updates)

        userRepository.updateUser(
            email = email,
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