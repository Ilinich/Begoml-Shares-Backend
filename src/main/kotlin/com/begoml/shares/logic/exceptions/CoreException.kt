package com.begoml.shares.logic.exceptions

sealed class CoreException(
    override val message: String,
    override val cause: Throwable?
) : Throwable(
    message = message,
    cause = cause,
)

/** Transport-agnostic error category; the presentation layer maps it to an HTTP status. */
enum class ErrorKind {
    BAD_REQUEST,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
}

data class CommonException(
    val kind: ErrorKind,
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
