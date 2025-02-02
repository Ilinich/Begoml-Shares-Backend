package com.begoml.shares.data.database.models.shares

import com.begoml.shares.logic.repositories.shares.Share
import com.begoml.shares.tools.AppCurrency
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import java.util.*

internal object ShareData : LongIdTable() {
    val shareId: Column<Long> = long("shareId").uniqueIndex()
    val symbol: Column<String> = varchar("symbol", 100).uniqueIndex()

    val name: Column<String> = varchar("name", 200)
    val description: Column<String> = text("description")

    val logoUrl: Column<String> = varchar("logoUrl", 1000)
    val imageUrl: Column<String> = varchar("imageUrl", 1000)

    val status: Column<String> = varchar("status", 20).default("ACTIVE")

    fun toDomain(row: ResultRow): Share {
        return Share(
            shareId = row[shareId],
            symbol = row[symbol],
            name = row[name],
            description = row[description],
            logoUrl = row[logoUrl],
            imageUrl = row[imageUrl],
            currency = AppCurrency.USD,
        )
    }
}