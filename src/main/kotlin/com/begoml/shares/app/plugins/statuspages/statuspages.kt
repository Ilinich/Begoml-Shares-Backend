package com.begoml.shares.app.plugins.statuspages

import com.begoml.shares.logic.exceptions.CommonException
import com.begoml.shares.logic.exceptions.RateLimitException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.statusPages() {
    install(StatusPages) {
        authStatusPages()

        exception<CommonException> { call, cause ->
            call.respondText(
                text = cause.message,
                contentType = ContentType.Text.Plain,
                status = cause.status
            )
        }

        // Ktor's own 4xx exceptions (e.g. malformed JSON in call.receive) must not fall into the 500 handler.
        exception<BadRequestException> { call, _ ->
            call.respondText(
                text = "Bad Request",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            )
        }

        exception<NotFoundException> { call, _ ->
            call.respondText(
                text = "Not Found",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.NotFound,
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respondText(
                text = "Internal server error",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.InternalServerError
            )
        }

        exception<IllegalArgumentException> { call, _ ->
            call.respondText(
                text = "Bad Request",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            )
        }

        exception<RateLimitException> { call, cause ->
            call.respondText(
                text = cause.message ?: "Too many requests",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.TooManyRequests,
            )
        }
    }
}