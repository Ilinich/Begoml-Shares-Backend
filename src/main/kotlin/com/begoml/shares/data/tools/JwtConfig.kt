package com.begoml.shares.data.tools

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.tools.USER_ID_KEY
import java.util.*

class JwtConfig(secret: String) {

    companion object {
        private const val ISSUER = "com.begoml.shares"
        private const val VALIDITY_IN_MS = 24L * 60 * 60 * 1000 // 1 day
    }

    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .build()

    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(ISSUER)
        .withJWTId(UUID.randomUUID().toString())
        .withClaim(USER_ID_KEY, user.id)
        .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_IN_MS))
        .withIssuedAt(Date())
        .sign(algorithm)

    fun decodeJWT(token: String): DecodedJWT = verifier.verify(token)
}
