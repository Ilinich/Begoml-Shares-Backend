package com.begoml.shares.data.database.models

import com.begoml.shares.logic.services.user.models.UserProfile
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

internal object UserProfileData : LongIdTable() {
    val userId: Column<String> = reference("userId", UserData.userId).uniqueIndex() // внешний ключ на UserData
    val username: Column<String> = varchar("username", 100) // имя пользователя
    val age: Column<Int?> = integer("age").nullable() // возраст пользователя

    fun toDomain(row: ResultRow): UserProfile {
        return UserProfile(
            userId = row[userId],
            name = row[username],
            age = row[age]
        )
    }
}