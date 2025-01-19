package com.begoml.shares.presentation.features.shares

import com.begoml.shares.tools.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ShareRemoteResponse(
    val shareId: Long,
    val symbol: String,

    val name: String,

    val logoUrl: String,

    val currency: String,

    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
)

@Serializable
data class ShareInfoRemoteResponse(
    val shareId: Long,
    val symbol: String,

    val name: String,
    val description: String,

    val logoUrl: String,
    val imageUrl: String,

    val currency: String,

    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
)