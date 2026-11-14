package com.begoml.shares.data.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Runs [block] in a transaction on [Dispatchers.IO], so JDBC calls do not block
 * the request-handling threads that call suspend repository functions.
 */
internal suspend fun <T> dbQuery(db: Database, block: suspend Transaction.() -> T): T =
    newSuspendedTransaction(context = Dispatchers.IO, db = db, statement = block)
