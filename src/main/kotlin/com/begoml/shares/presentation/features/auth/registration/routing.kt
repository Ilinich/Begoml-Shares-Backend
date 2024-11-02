package com.begoml.shares.presentation.features.auth.registration

import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.registrationRoutes() {

    val controller by inject<RegistrationController>()

    post("/registration") {
        controller.registration(context)
    }
}