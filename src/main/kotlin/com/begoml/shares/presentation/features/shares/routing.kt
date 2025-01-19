package com.begoml.shares.presentation.features.shares

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.sharesRoutes() {
    val controller by inject<SharesController>()

    authenticate {
        get("/shares") {
            controller.getShares(call)
        }

        get("/shareInfo") {
            controller.getShareInfo(call)
        }
    }
}