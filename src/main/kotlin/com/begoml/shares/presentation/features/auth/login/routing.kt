package com.begoml.shares.presentation.features.auth.login

import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.loginRoutes() {

    val loginController by inject<LoginController>()

    post("/login") {
        loginController.loginByEmail(context)
    }
}