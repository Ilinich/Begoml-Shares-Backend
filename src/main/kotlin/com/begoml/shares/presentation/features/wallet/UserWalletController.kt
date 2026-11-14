package com.begoml.shares.presentation.features.wallet

import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.services.confirmation.ConfirmationCodeService
import com.begoml.shares.logic.services.wallet.UserWalletService
import com.begoml.shares.presentation.exceptions.handleExceptions
import com.begoml.shares.presentation.tools.safeUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class UserWalletController(
    private val walletService: UserWalletService,
    private val confirmationCodeService: ConfirmationCodeService,
) {

    suspend fun getWallet(call: ApplicationCall) {
        call.handleExceptions {
            val userId = call.safeUser.userId

            val wallet = walletService.getWallet(userId)
            call.respond(wallet.toUserWalletResponse())
        }
    }

    suspend fun createWallet(call: ApplicationCall) {
        call.handleExceptions {
            val userId = call.safeUser.userId
            val params = call.receive<UserWalletNameRemoteParams>()

            require(walletService.findWallet(userId) == null)

            walletService.createWallet(userId = userId, name = params.name)

            val wallet = walletService.getWallet(userId)

            call.respond(HttpStatusCode.Created, wallet.toUserWalletResponse())
        }
    }

    suspend fun updateName(call: ApplicationCall) {
        call.handleExceptions {
            val userId = call.safeUser.userId
            val params = call.receive<UserWalletNameRemoteParams>()

            walletService.updateName(
                userId = userId,
                newWalletName = params.name,
            )

            call.respond(HttpStatusCode.OK)
        }
    }

    suspend fun deposit(call: ApplicationCall) {
        call.handleExceptions {
            val userId = call.safeUser.userId
            val params = call.receive<UserWalletDepositRemoteParams>()

            walletService.checkRateLimit(userId)

            if (!verifyConfirmationCode(userId = userId, confirmationCode = params.confirmationCode)) {
                call.respond(HttpStatusCode.Forbidden, "Invalid confirmation code")
                return@handleExceptions
            }

            walletService.deposit(userId = userId, amount = params.amount)
            call.respond(HttpStatusCode.OK, "Deposit successful")
        }
    }

    suspend fun withdraw(call: ApplicationCall) {
        call.handleExceptions {
            val userId = call.safeUser.userId
            val params = call.receive<UserWalletWithdrawRemoteParams>()

            walletService.checkRateLimit(userId)

            if (!verifyConfirmationCode(userId = userId, confirmationCode = params.confirmationCode)) {
                call.respond(HttpStatusCode.Forbidden, "Invalid confirmation code")
                return@handleExceptions
            }

            walletService.withdraw(userId = userId, amount = params.amount)
            call.respond(HttpStatusCode.OK, "Withdrawal successful")
        }
    }

    private suspend fun verifyConfirmationCode(userId: String, confirmationCode: String): Boolean {
        return confirmationCodeService.validateCode(userId = userId, confirmationCode = confirmationCode)
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