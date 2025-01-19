package com.begoml.shares.app.plugins.routing

import com.begoml.shares.presentation.features.auth.login.loginRoutes
import com.begoml.shares.presentation.features.auth.registration.registrationRoutes
import com.begoml.shares.presentation.features.shares.sharesRoutes
import com.begoml.shares.presentation.features.user.userRoutes
import com.begoml.shares.presentation.features.wallet.walletRoutes
import com.begoml.shares.presentation.features.watchlist.watchListRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        swaggerUI(
            path = "swagger",
            swaggerFile = "openapi/documentation.yaml",
        )

        loginRoutes()
        registrationRoutes()
        userRoutes()
        walletRoutes()
        sharesRoutes()
        watchListRoutes()
    }
}
