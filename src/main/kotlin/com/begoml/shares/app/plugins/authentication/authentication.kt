package com.begoml.shares.app.plugins.authentication

import com.begoml.shares.data.tools.JwtConfig
import com.begoml.shares.presentation.tools.UserPrincipal
import com.begoml.shares.tools.USER_ID_KEY
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.authentication() {
    val jwtConfig by inject<JwtConfig>()

    install(Authentication) {
        jwt {
            verifier(jwtConfig.verifier)
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