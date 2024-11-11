package com.begoml.shares.data.tools

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.begoml.shares.logic.services.user.models.User
import com.begoml.shares.tools.USER_ID_KEY
import java.util.*

object JwtConfig {

    private const val secret = "begoml-shares-secret-key" // System.getenv("JWT_SECRET") ?: "app-token-secret-key"

    private const val issuer = "com.begoml.shares"

    private const val validityInMs = 36_000_00 * 24 // 1 day

    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Produce a token for this combination of name and password
     */
    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withJWTId(UUID.randomUUID().toString())
        .withClaim(USER_ID_KEY, user.id)
        .withExpiresAt(getExpiration())
        .withIssuedAt(Date())
        .sign(algorithm)

    fun decodeJWT(token: String): DecodedJWT = JWT.require(algorithm)
        .build()
        .verify(token)

    /**
     * Calculate the expiration Date based on current time + the given validity
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}