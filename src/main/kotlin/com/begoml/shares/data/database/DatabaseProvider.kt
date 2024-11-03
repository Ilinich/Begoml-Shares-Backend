package com.begoml.shares.data.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.math.absoluteValue

class DatabaseProvider(
    private val shards: List<Database>
) {

    init {
        repeat(shards.size) { index ->
            transaction(shards[index]) {
                SchemaUtils.create(UserData)
            }
        }
    }

    fun getDatabase(userId: String): Database {
        val shardIndex = UUID.fromString(userId).hashCode().absoluteValue % shards.size
        return shards[shardIndex]
    }
}