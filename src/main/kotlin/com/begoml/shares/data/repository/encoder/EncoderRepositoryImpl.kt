package com.begoml.shares.data.repository.encoder

import com.begoml.shares.data.tools.Cipher
import com.begoml.shares.logic.repositories.encoder.EncoderRepository
import java.util.*

class EncoderRepositoryImpl(
    private val base64Encoder: Base64.Encoder = Base64.getEncoder()
) : EncoderRepository {

    override fun encrypt(value: String): String {
        return String(base64Encoder.encode(encryptValue(value)))
    }

    private fun encryptValue(data: String?): ByteArray = Cipher.algorithm.sign(data?.toByteArray())
}