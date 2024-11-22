package com.begoml.shares.app.plugins.statuspages

import com.begoml.shares.logic.exceptions.AuthenticationException
import com.begoml.shares.logic.exceptions.AuthorizationException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun StatusPagesConfig.authStatusPages() {
    exception<AuthenticationException> { call, cause ->
        call.respondText(
            text = cause.message,
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.Unauthorized,
        )
    }
    exception<AuthorizationException> { call, cause ->
        call.respondText(
            text = cause.message,
            contentType = ContentType.Text.Plain,
            status = HttpStatusCode.Forbidden,
        )
    }
}