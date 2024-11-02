package com.begoml.shares.data.tools

import com.auth0.jwt.algorithms.Algorithm

object Cipher {
    private const val secret = "begoml-shares-secret-key" // System.getenv("JWT_SECRET") ?: "app-secret-key"

    val algorithm = Algorithm.HMAC256(secret)

    fun encrypt(data: String?): ByteArray = algorithm.sign(data?.toByteArray())
}
