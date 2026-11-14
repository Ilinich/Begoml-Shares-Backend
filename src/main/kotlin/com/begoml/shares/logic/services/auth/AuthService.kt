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
    // Checked when the email is unknown, so "no such user" costs as much time as "wrong password".
    private val dummyHash: String by lazy { encoderRepository.hash("dummy-password") }

    suspend fun createUserByEmail(email: String, password: String): String {
        validateAuthData(email = email, password = password)

        val searchUser = userRepository.findUserByEmail(email)

        if (searchUser != null) {
            throw CommonException(
                status = HttpStatusCode.BadRequest,
                message = "Email already registered!",
            )
        }

        val user = userRepository.createUser(
            email = email,
            password = encoderRepository.hash(password),
        )

        return generateJwtToken(user)
    }

    suspend fun loginByEmail(email: String, password: String): String {
        val user = userRepository.findUserByEmail(email = email)

        val passwordValid = if (user != null) {
            encoderRepository.verify(password, user.password)
        } else {
            encoderRepository.verify(password, dummyHash)
            false
        }

        if (user == null || !passwordValid) {
            throw CommonException(
                status = HttpStatusCode.BadRequest,
                message = "Invalid email or password",
            )
        }

        if (encoderRepository.needsRehash(user.password)) {
            userRepository.updatePassword(userId = user.id, password = encoderRepository.hash(password))
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
