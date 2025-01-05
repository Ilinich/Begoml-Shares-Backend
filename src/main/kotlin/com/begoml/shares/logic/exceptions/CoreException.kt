package com.begoml.shares.logic.exceptions

import io.ktor.http.*

sealed class CoreException(
    override val message: String,
    override val cause: Throwable?
) : Throwable(
    message = message,
    cause = cause,
)

data class CommonException(
    val status: HttpStatusCode,
    override val message: String,
    override val cause: Throwable? = null
) : CoreException(message, cause)

data class AuthenticationException(
    override val message: String = "Authentication failed"
) : Throwable(message = message)

data class AuthorizationException(
    override val message: String = "You are not authorised to use this service"
) : Throwable(message = message)

class InternalServerErrorException(message: String) : Throwable(message = message)

class RateLimitException(message: String) : Throwable(message = message)