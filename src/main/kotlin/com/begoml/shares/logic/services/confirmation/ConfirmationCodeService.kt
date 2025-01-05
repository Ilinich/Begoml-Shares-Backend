package com.begoml.shares.logic.services.confirmation

class ConfirmationCodeService {

    suspend fun validateCode(userId: String, confirmationCode: String): Boolean {
        // TODO will be logic for verify confirmation code
        return confirmationCode == "333"
    }
}