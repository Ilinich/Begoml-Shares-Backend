package com.begoml.shares.presentation.features.auth.registration

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationParams(
    val login: String,
    val password: String,
)

@Serializable
data class RegistrationRemoteResponse(
    val token: String,
)