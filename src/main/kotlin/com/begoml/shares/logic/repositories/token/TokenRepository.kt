package com.begoml.shares.logic.repositories.token

import com.begoml.shares.logic.services.user.models.User

interface TokenRepository {

    fun generateJwtToken(user: User): String?
}
