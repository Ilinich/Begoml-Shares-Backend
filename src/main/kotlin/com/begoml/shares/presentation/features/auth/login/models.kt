package com.begoml.shares.presentation.features.auth.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginParams(
    val login: String,
    val password: String,
)

@Serializable
data class LoginRemoteResponse(
    val token: String,
)