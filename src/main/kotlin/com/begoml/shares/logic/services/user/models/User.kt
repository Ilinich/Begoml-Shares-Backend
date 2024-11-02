package com.begoml.shares.logic.services.user.models

data class User(
    val id: Long,
    val email: String,
    val name: String,
    val age: Int?,
    val password: String,
)
