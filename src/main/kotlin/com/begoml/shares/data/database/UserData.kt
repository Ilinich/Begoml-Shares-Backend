package com.begoml.shares.data.database

import com.begoml.shares.logic.services.user.models.User
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

internal object UserData : LongIdTable() {
    val email: Column<String> = varchar("email", 200).uniqueIndex()
    val username: Column<String> = varchar("username", 100)
    val password: Column<String> = varchar("password", 150)
    val age: Column<Int?> = integer("age").nullable()

    fun toDomain(row: ResultRow): User {
        return User(
            id = row[id].value,
            email = row[email],
            password = row[password],
            name = row[username],
            age = row[age],
        )
    }
}