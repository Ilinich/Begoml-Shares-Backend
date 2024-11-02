package com.begoml.shares.app.plugins.authentication

import com.begoml.shares.logic.repositories.token.TokenRepository
import com.begoml.shares.presentation.tools.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.authentication() {
    val tokenRepository by inject<TokenRepository>()

    install(Authentication) {
        jwt {
            verifier(tokenRepository.verifier)
            realm = "com.begoml.shares"

            validate { payloadHolder ->
                val email = payloadHolder.payload.getClaim("email").asString()
                if (email != null) {
                    UserPrincipal(email = email)
                } else {
                    null
                }
            }
        }
    }
}