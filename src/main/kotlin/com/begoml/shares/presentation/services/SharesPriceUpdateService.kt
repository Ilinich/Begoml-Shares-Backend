package com.begoml.shares.presentation.services

import com.begoml.shares.logic.repositories.shares.SharesRepository
import com.begoml.shares.presentation.tools.RabbitEvents
import com.begoml.shares.tools.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import mu.KLogger

class SharesPriceUpdateService(
    private val scope: CoroutineScope,
    private val logger: KLogger,
    private val sharesRepository: SharesRepository,
    private val rabbitEmitter: RabbitEmitter
) {

    private var job: Job? = null

    fun update() {
        job?.cancel()

        job = scope.safeLaunch(
            handleException = { throwable ->
                logger.error("SharesPriceUpdateService: ${throwable.message}", throwable)
            }
        ) {
            if (!sharesRepository.hasShares()) {
                sharesRepository.updateShares()
            } else {
                sharesRepository.updateSharePrices()
            }

            rabbitEmitter.emit(RabbitEvents.SharePricesUpdated)
        }
    }
}