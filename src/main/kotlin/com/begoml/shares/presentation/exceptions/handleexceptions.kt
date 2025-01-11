package com.begoml.shares.presentation.exceptions

import com.begoml.shares.logic.exceptions.AuthenticationException
import com.begoml.shares.logic.exceptions.AuthorizationException
import io.ktor.server.application.*
import mu.KotlinLogging

val logger = KotlinLogging.logger {}

suspend fun <T> ApplicationCall.handleExceptions(block: suspend () -> T): T? {
    return try {
        block()
    } catch (error: AuthenticationException) {
        logger.error(error.message, error)
        throw error
    } catch (error: AuthorizationException) {
        logger.error(error.message, error)
        throw error
    } catch (error: Throwable) {
        logger.error("Unexpected error: ${error.message}", error)
        throw error
    }
}