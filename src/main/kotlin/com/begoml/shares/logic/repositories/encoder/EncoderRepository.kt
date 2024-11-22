package com.begoml.shares.logic.repositories.encoder

interface EncoderRepository {

    fun encrypt(value: String): String
}