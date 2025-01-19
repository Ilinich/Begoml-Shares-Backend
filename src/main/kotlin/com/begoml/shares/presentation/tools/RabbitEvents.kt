package com.begoml.shares.presentation.tools

import kotlinx.serialization.Serializable

@Serializable
sealed interface RabbitEvents {

    @Serializable
    data object SharePricesUpdated : RabbitEvents
}