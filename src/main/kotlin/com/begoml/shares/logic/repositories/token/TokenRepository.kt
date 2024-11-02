package com.begoml.shares.logic.repositories.token

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.interfaces.DecodedJWT
import com.begoml.shares.logic.services.user.models.User

interface TokenRepository {

    val verifier: JWTVerifier

    fun decodeJWT(token: String): DecodedJWT

    fun generateJwtToken(user: User): String?
}