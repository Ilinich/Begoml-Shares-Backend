package com.begoml.shares.presentation.features.auth.registration

import com.begoml.shares.logic.services.auth.AuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class RegistrationController(
    private val authService: AuthService,
) {
    suspend fun registration(call: ApplicationCall) {
        val params = call.receive<RegistrationParams>()
        val token = authService.createUserByEmail(
            email = params.login,
            password = params.password,
        )
        call.respond(RegistrationRemoteResponse(token))
    }
}