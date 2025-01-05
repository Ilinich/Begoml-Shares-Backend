package com.begoml.shares.presentation.features.user

import com.begoml.shares.logic.services.user.UserService
import com.begoml.shares.presentation.exceptions.handleExceptions
import com.begoml.shares.presentation.tools.safeUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class UserController(
    private val userService: UserService,
) {
    suspend fun getUser(call: ApplicationCall) {
        call.handleExceptions {
            val userId = call.safeUser.userId

            val user = userService.getUser(userId = userId)
            val userProfile = userService.getUserProfile(userId = userId)

            call.respond(
                UserRemoteResponse(
                    id = userId,
                    email = user.email,
                    name = userProfile?.name,
                    age = userProfile?.age,
                )
            )
        }
    }

    suspend fun updateUser(call: ApplicationCall) {
        call.handleExceptions {
            val updates = call.receive<UpdateUserParams>().toUserUpdates()
            userService.updateUser(
                userId = call.safeUser.userId,
                updates = updates,
            )
            call.respond(HttpStatusCode.OK)
        }
    }
}