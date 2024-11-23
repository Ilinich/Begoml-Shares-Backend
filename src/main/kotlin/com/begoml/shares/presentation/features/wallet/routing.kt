package com.begoml.shares.presentation.features.wallet

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.walletRoutes() {
    val controller by inject<UserWalletController>()

    authenticate {
        get("/wallet") {
            controller.getWallet(context)
        }

        post("/wallet/create") {
            controller.createWallet(context)
        }
    }
}