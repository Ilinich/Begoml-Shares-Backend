package com.begoml.shares.app.plugins.statuspages

import com.begoml.shares.logic.exceptions.CommonException
import io.ktor.http.*
import io.ktor.server.application.*
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

        exception<UnknownError> { call, _ ->
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
    }
}