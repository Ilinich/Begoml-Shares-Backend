package com.begoml.shares.data.repository.encoder

import com.auth0.jwt.algorithms.Algorithm
import com.begoml.shares.logic.repositories.encoder.EncoderRepository
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.util.*

class EncoderRepositoryImpl(
    legacySecret: String,
    private val logRounds: Int = DEFAULT_LOG_ROUNDS,
) : EncoderRepository {

    companion object {
        private const val DEFAULT_LOG_ROUNDS = 12
        private const val BCRYPT_PREFIX = "$2"
    }

    private val legacyAlgorithm = Algorithm.HMAC256(legacySecret)

    override fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(logRounds))

    override fun verify(password: String, hash: String): Boolean {
        return if (isBcrypt(hash)) {
            runCatching { BCrypt.checkpw(password, hash) }.getOrDefault(false)
        } else {
            MessageDigest.isEqual(legacyHash(password).toByteArray(), hash.toByteArray())
        }
    }

    override fun needsRehash(hash: String): Boolean = !isBcrypt(hash)

    private fun isBcrypt(hash: String) = hash.startsWith(BCRYPT_PREFIX)

    // Pre-bcrypt scheme: Base64(HMAC-SHA256(password)). Kept only to migrate existing users.
    private fun legacyHash(password: String): String =
        Base64.getEncoder().encodeToString(legacyAlgorithm.sign(password.toByteArray()))
}
