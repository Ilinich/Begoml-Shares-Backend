package com.begoml.shares.data.database.models.user

import com.begoml.shares.logic.services.user.models.User
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

internal object UserData : LongIdTable() {
    val userId: Column<String> = varchar("userId", 200).uniqueIndex()
    val email: Column<String> = varchar("email", 200).uniqueIndex()
    val password: Column<String> = varchar("password", 150)

    fun toDomain(row: ResultRow): User {
        return User(
            id = row[userId],
            email = row[email],
            password = row[password],
        )
    }
}