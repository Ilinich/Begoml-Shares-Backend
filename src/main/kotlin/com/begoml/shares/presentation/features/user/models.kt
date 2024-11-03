package com.begoml.shares.presentation.features.user

import com.begoml.shares.logic.services.user.models.params.UserUpdates
import kotlinx.serialization.Serializable

@Serializable
data class UserRemoteResponse(
    val id: String,
    val email: String,
    val name: String,
    val age: Int?,
)

@Serializable
data class UpdateUserParams(
    val name: String? = null,
    val age: Int? = null,
)

fun UpdateUserParams.toUserUpdates() = UserUpdates(
    name = this.name,
    age = this.age,
)
