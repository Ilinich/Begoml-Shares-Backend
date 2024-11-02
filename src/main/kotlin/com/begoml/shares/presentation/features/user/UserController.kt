package com.begoml.shares.presentation.features.user

import com.begoml.shares.logic.services.user.UserService
import com.begoml.shares.presentation.tools.safeUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class UserController(
    private val userService: UserService,
) {
    suspend fun getUser(call: ApplicationCall) {
        val email = call.safeUser.email
        val user = userService.getUser(email = email)
        call.respond(
            UserRemoteResponse(
                id = user.id,
                email = user.email,
                name = user.name,
                age = user.age,
            )
        )
    }

    suspend fun updateUser(call: ApplicationCall) {
        val email = call.safeUser.email
        val updates = call.receive<UpdateUserParams>().toUserUpdates()
        userService.updateUser(
            email = email,
            updates = updates,
        )
        call.respond(HttpStatusCode.OK)
    }
}