package com.begoml.shares.presentation.features.user

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val controller by inject<UserController>()

    authenticate {
        get("/user") {
            controller.getUser(context)
        }

        post("/user/update") {
            controller.updateUser(context)
        }
    }
}