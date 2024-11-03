package com.begoml.shares.logic.services.auth

import com.begoml.shares.data.ext.isEmailValid
import com.begoml.shares.logic.exceptions.CommonException
import com.begoml.shares.logic.repositories.encoder.EncoderRepository
import com.begoml.shares.logic.repositories.token.TokenRepository
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.services.user.models.User
import io.ktor.http.*

class AuthService(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val encoderRepository: EncoderRepository,
) {
    suspend fun createUserByEmail(email: String, password: String): String {
        val searchUser = userRepository.findByUserId(email)

        if (searchUser != null) {
            throw CommonException(
                status = HttpStatusCode.BadRequest,
                message = "Email already registered!",
            )
        }

        validateAuthData(email = email, password = password)

        val user = userRepository.createUser(
            email = email,
            password = encoderRepository.encrypt(password),
        )

        return generateJwtToken(user)
    }

    suspend fun loginByEmail(email: String, password: String): String {
        val user = userRepository.findByUserId(email)
        checkNotNull(user)

        if (user.password != encoderRepository.encrypt(password)) {
            throw CommonException(
                status = HttpStatusCode.BadRequest,
                message = "Password invalid!",
            )
        }

        return generateJwtToken(user)
    }

    private fun generateJwtToken(user: User): String {
        return tokenRepository.generateJwtToken(user) ?: throw CommonException(
            status = HttpStatusCode.BadRequest,
            message = "Token can not be null",
        )
    }

    private fun validateAuthData(email: String, password: String) {
        require(
            email.isEmailValid() && password.isNotBlank()
        ) { "User is invalid." }
    }
}