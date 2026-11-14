package com.begoml.shares.data

import com.begoml.shares.data.database.DatabaseProvider
import org.jetbrains.exposed.sql.Database
import java.util.*

/** In-memory H2 databases standing in for the Postgres shards. */
internal fun testDatabaseProvider(shardCount: Int = 2): DatabaseProvider {
    fun connect(): Database = Database.connect(
        url = "jdbc:h2:mem:${UUID.randomUUID()};MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver",
    )
    return DatabaseProvider(
        physicalShards = List(shardCount) { connect() },
        shares = connect(),
    )
}
