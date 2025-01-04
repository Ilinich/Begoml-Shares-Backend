package com.begoml.shares.presentation.features.wallet

import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.services.wallet.UserWalletService
import com.begoml.shares.presentation.tools.safeUser
import io.ktor.server.application.*
import io.ktor.server.response.*

class UserWalletController(
    private val walletService: UserWalletService,
) {


    suspend fun getWallet(call: ApplicationCall) {
        val userId = call.safeUser.userId

        val wallet = walletService.getWallet(userId)
        call.respond(wallet.toUserWalletResponse())
    }

    suspend fun createWallet(call: ApplicationCall) {
        val userId = call.safeUser.userId

        require(walletService.findWallet(userId) == null)

        walletService.createWallet(userId)

        val wallet = requireNotNull(walletService.getWallet(userId))
        call.respond(wallet.toUserWalletResponse())
    }
}

private fun UserWallet.toUserWalletResponse(): UserWalletRemoteResponse {
    val wallet = this
    return UserWalletRemoteResponse(
        walletId = wallet.walletId,
        name = wallet.name,
        currency = wallet.currency,
        balances = wallet.balances,
        status = wallet.status.name,
        createdAt = wallet.createdAt,
    )
}