package com.begoml.shares.presentation.services

import com.begoml.shares.tools.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import mu.KLogger

class SharesNewsUpdateService(
    private val scope: CoroutineScope,
    private val logger: KLogger,
) {

    private var job: Job? = null

    fun update() {
        job?.cancel()

        job = scope.safeLaunch(
            handleException = { throwable ->
                logger.error("SharesNewsUpdateService: ${throwable.message}", throwable)
            }
        ) {
            // TODO will be logic to load new news
        }
    }
}