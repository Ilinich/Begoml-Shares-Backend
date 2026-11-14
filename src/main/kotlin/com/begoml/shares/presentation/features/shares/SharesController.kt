package com.begoml.shares.presentation.features.shares

import com.begoml.shares.logic.exceptions.CommonException
import com.begoml.shares.logic.exceptions.ErrorKind
import com.begoml.shares.logic.repositories.shares.Share
import com.begoml.shares.logic.repositories.shares.SharesRepository
import com.begoml.shares.presentation.exceptions.handleExceptions
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.math.BigDecimal

class SharesController(
    private val sharesRepository: SharesRepository
) {

    suspend fun getShares(call: ApplicationCall) {
        call.handleExceptions {
            val prices = sharesRepository.getPrices()
            val shares = sharesRepository.getShares()

            val sharesResponse = shares.map { share ->
                val price = prices[share.symbol] ?: BigDecimal.ZERO

                share.toShareResponse(price = price)
            }
            call.respond(HttpStatusCode.OK, sharesResponse)
        }
    }

    suspend fun getShareInfo(call: ApplicationCall) {
        call.handleExceptions {
            val symbol = requireNotNull(call.parameters["symbol"])

            val price = sharesRepository.getPrice(symbol) ?: BigDecimal.ZERO

            val share = sharesRepository.getShare(symbol) ?: throw CommonException(
                kind = ErrorKind.NOT_FOUND,
                message = "Share not found",
            )
            val info = share.toShareInfoResponse(price = price)

            call.respond(HttpStatusCode.OK, info)
        }
    }

    suspend fun getSharesPrices(call: ApplicationCall) {
        call.handleExceptions {
            val prices = sharesRepository.getPrices()

            val newPrices = prices.map {
                SharePriceResponse(
                    symbol = it.key,
                    price = it.value,
                )
            }

            call.respond(HttpStatusCode.OK, newPrices)
        }
    }
}

private fun Share.toShareResponse(price: BigDecimal): ShareRemoteResponse {
    val share = this
    return ShareRemoteResponse(
        shareId = share.shareId,
        symbol = share.symbol,
        name = share.name,
        currency = share.currency,
        logoUrl = share.logoUrl,
        price = price,
    )
}

private fun Share.toShareInfoResponse(price: BigDecimal): ShareInfoRemoteResponse {
    val share = this
    return ShareInfoRemoteResponse(
        shareId = share.shareId,
        symbol = share.symbol,
        name = share.name,
        description = share.description,
        currency = share.currency,
        logoUrl = share.logoUrl,
        imageUrl = share.imageUrl,
        price = price,
    )
}