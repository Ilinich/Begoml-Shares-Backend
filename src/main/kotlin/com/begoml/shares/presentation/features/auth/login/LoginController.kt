package com.begoml.shares.presentation.features.auth.login

import com.begoml.shares.logic.services.auth.AuthService
import com.begoml.shares.presentation.exceptions.handleExceptions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class LoginController(
    private val authService: AuthService,
) {
    suspend fun loginByEmail(call: ApplicationCall) {
        call.handleExceptions {
            val params = call.receive<LoginParams>()
            val token = authService.loginByEmail(
                email = params.login,
                password = params.password,
            )
            call.respond(HttpStatusCode.OK, LoginRemoteResponse(token))
        }
    }
}