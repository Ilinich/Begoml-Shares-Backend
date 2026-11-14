package com.begoml.shares.data

import com.auth0.jwt.algorithms.Algorithm
import com.begoml.shares.data.repository.encoder.EncoderRepositoryImpl
import java.util.*
import kotlin.test.*

class EncoderRepositoryImplTest {

    private val legacySecret = "legacy-secret"
    private val encoder = EncoderRepositoryImpl(legacySecret = legacySecret, logRounds = 4)

    @Test
    fun `bcrypt hashes are salted and verifiable`() {
        val first = encoder.hash("password")
        val second = encoder.hash("password")

        assertNotEquals(first, second)
        assertTrue(encoder.verify("password", first))
        assertFalse(encoder.verify("wrong", first))
        assertFalse(encoder.needsRehash(first))
    }

    @Test
    fun `legacy HMAC hashes still verify and are flagged for rehash`() {
        val legacy = Base64.getEncoder()
            .encodeToString(Algorithm.HMAC256(legacySecret).sign("password".toByteArray()))

        assertTrue(encoder.verify("password", legacy))
        assertFalse(encoder.verify("wrong", legacy))
        assertTrue(encoder.needsRehash(legacy))
    }
}
