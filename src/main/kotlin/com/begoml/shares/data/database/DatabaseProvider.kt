package com.begoml.shares.data.database

import com.begoml.shares.data.database.models.shares.ShareData
import com.begoml.shares.data.database.models.user.UserData
import com.begoml.shares.data.database.models.user.UserProfileData
import com.begoml.shares.data.database.models.wallet.UserWalletData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import kotlin.math.absoluteValue

/**
 * `DatabaseProvider` is responsible for managing and selecting the appropriate database shard
 * based on a virtual sharding mechanism. The provider supports a configurable number of physical shards
 * and a set number of virtual shards, providing an abstraction over physical databases.
 *
 * This implementation is designed for educational purposes to demonstrate virtual sharding.
 * In a production-grade system with millions of users, it's recommended to use existing, robust
 * sharding solutions or services, such as cloud-based databases with native sharding support
 * or specialized sharding middleware, as manual sharding may lead to significant challenges
 * with data migration and scaling.
 *
 * In this example, we configure the `DatabaseProvider` to use 32 virtual shards, which is
 * a reasonable starting point for handling up to 10 million users, assuming data is distributed
 * evenly. However, it is important to note that scaling this solution in the future may require
 * redistributing data and increasing the number of virtual shards, which can be complex and require
 * migration strategies. These strategies are not implemented in this simple solution.
 *
 * The number of virtual shards (32) is chosen based on the assumption that this will be sufficient
 * for handling the initial scale of users and provides a balance between efficient data distribution
 * and manageable complexity. However, if the number of users grows beyond 10 million, this architecture
 * would need to be reconsidered, and migration to more advanced distributed database systems should be
 * considered.
 *
 * @param physicalShards A list of physical databases where the data is stored.
 * @param virtualShardCount The number of virtual shards to distribute the data across.
 *        Default value is 32, which is suitable for handling a large number of users with good
 *        performance.
 *
 * @constructor Initializes the `DatabaseProvider` and creates necessary database schemas for each physical shard.
 */
class DatabaseProvider(
    private val physicalShards: List<Database>,
    private val shares: Database,
    private val virtualShardCount: Int = 32,
) {

    private val virtualShardMapping: Map<Int, Database> = (0 until virtualShardCount)
        .associateWith { virtualShard ->
            physicalShards[virtualShard % physicalShards.size]
        }

    init {
        physicalShards.forEach { db ->
            transaction(db) {
                SchemaUtils.create(
                    UserData,
                    UserProfileData,
                    UserWalletData,
                )
            }
        }

        transaction(shares) {
            SchemaUtils.create(ShareData)
        }
    }

    fun getShards(): List<Database> {
        return physicalShards
    }

    fun getShares(): Database {
        return shares
    }

    fun getDatabase(userId: String): Database {
        val virtualShardIndex = hashToShardIndex(userId, virtualShardCount)
        return virtualShardMapping[virtualShardIndex]
            ?: error("Invalid virtual shard index: $virtualShardIndex")
    }

    private fun hashToShardIndex(value: String, shardCount: Int): Int {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(value.toByteArray())
        val hashCode = hashBytes.fold(0) { acc, byte -> (acc * 31 + byte.toInt()) }
        return hashCode.absoluteValue % shardCount
    }
}