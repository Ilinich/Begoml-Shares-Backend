package com.begoml.shares.app.plugins.authentication

import com.begoml.shares.logic.repositories.token.TokenRepository
import com.begoml.shares.presentation.tools.UserPrincipal
import com.begoml.shares.tools.USER_ID_KEY
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
                val userId = payloadHolder.payload.getClaim(USER_ID_KEY).asString()
                if (userId != null) {
                    UserPrincipal(userId = userId)
                } else {
                    null
                }
            }
        }
    }
}