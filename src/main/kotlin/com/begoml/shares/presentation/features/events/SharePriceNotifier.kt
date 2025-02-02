package com.begoml.shares.presentation.features.events

import com.begoml.shares.logic.repositories.shares.SharesRepository
import com.begoml.shares.presentation.exceptions.logger
import com.begoml.shares.presentation.services.RabbitConsumer
import com.begoml.shares.presentation.tools.RabbitEvents
import com.begoml.shares.tools.safeLaunch
import kotlinx.coroutines.CoroutineScope

class SharePriceNotifier(
    private val scope: CoroutineScope,
    private val sharesRepository: SharesRepository,
    private val webSocketManager: WebSocketManager,
    private val rabbitConsumer: RabbitConsumer,
) {
    fun startListening() {
        scope.safeLaunch(
            handleException = { throwable ->
                logger.error(throwable.message, throwable)
            }
        ) {
            rabbitConsumer.consumeMessages { message ->
                if (message == RabbitEvents.SharePricesUpdated) {
                    val prices = sharesRepository.getPrices()
                    webSocketManager.broadcastMessageNewSharesPrices(prices = prices)
                }
            }
        }
    }
}