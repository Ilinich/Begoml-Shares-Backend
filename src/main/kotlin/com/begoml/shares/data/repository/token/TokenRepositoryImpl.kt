package com.begoml.shares.data.repository.token

import com.auth0.jwt.JWTVerifier
import com.begoml.shares.data.tools.JwtConfig
import com.begoml.shares.logic.repositories.token.TokenRepository
import com.begoml.shares.logic.services.user.models.User

class TokenRepositoryImpl(
    private val jwtConfig: JwtConfig,
) : TokenRepository {

    override val verifier: JWTVerifier
        get() = jwtConfig.verifier

    override fun decodeJWT(token: String) = jwtConfig.decodeJWT(token)

    override fun generateJwtToken(user: User) = jwtConfig.generateToken(user)
}
