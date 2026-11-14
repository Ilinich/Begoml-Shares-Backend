package com.begoml.shares.logic.repositories.encoder

interface EncoderRepository {

    /** Produces a salted, slow hash suitable for storing a password. */
    fun hash(password: String): String

    fun verify(password: String, hash: String): Boolean

    /** `true` when [hash] uses an outdated scheme and should be replaced after a successful login. */
    fun needsRehash(hash: String): Boolean
}
