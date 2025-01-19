package com.begoml.shares.presentation.features.watchlist

import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Route.watchListRoutes() {

    authenticate {
        post("/watchList/add") {
        }

        post("/watchList/remove") {
        }
    }
}