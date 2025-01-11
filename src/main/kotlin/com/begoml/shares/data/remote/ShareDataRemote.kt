package com.begoml.shares.data.remote

import com.begoml.shares.tools.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ShareDataRemote(
    @SerialName("id")
    val shareId: Long? = null,

    @SerialName("symbol")
    val symbol: String? = null,

    val name: String? = null,

    val description: String? = null,

    val logoUrl: String? = null,

    val imageUrl: String? = null,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("price")
    val price: BigDecimal? = BigDecimal.ZERO,
)
