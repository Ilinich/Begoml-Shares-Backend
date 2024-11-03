package com.begoml.shares.presentation.tools

import io.ktor.server.application.*
import io.ktor.server.auth.*

val ApplicationCall.safeUser
    get() = requireNotNull(authentication.principal<UserPrincipal>()) {
        "Token is broken!"
    }

data class UserPrincipal(
    val userId: String,
    val email: String,
) : Principal