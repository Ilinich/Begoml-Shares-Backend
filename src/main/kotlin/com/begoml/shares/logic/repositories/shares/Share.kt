package com.begoml.shares.logic.repositories.shares

data class Share(
    val shareId: Long,
    val symbol: String,
    val name: String,
    val description: String,
    val logoUrl: String,
    val imageUrl: String,
    val currency: String,
)
