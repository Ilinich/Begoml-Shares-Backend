package com.begoml.shares.presentation.exceptions

import com.begoml.shares.logic.exceptions.AuthenticationException
import com.begoml.shares.logic.exceptions.AuthorizationException
import com.begoml.shares.logic.exceptions.InternalServerErrorException
import io.ktor.server.application.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

suspend fun <T> ApplicationCall.handleExceptions(block: suspend () -> T): T? {
    return try {
        block()
    } catch (error: AuthenticationException) {
        logger.warn(error.message, error)
        throw error
    } catch (error: AuthorizationException) {
        logger.warn(error.message, error)
        throw error
    } catch (error: Throwable) {
        logger.error("Unexpected error: ${error.message}", error)
        throw InternalServerErrorException("An unexpected error occurred")
    }
}