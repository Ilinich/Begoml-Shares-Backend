package com.begoml.shares.data

import com.begoml.shares.data.repository.user.UserRepositoryImpl
import com.begoml.shares.logic.services.user.models.params.UserUpdates
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class UserRepositoryImplTest {

    private val users = UserRepositoryImpl(testDatabaseProvider(shardCount = 2))

    @Test
    fun `findUserByEmail finds users on every shard`() = runBlocking {
        // 40 users spread over both shards: with the old firstOrNull() roughly half were not found.
        val emails = (1..40).map { "user$it@test.com" }
        emails.forEach { users.createUser(email = it, password = "hash") }

        emails.forEach { email ->
            assertEquals(email, users.findUserByEmail(email)?.email, "not found: $email")
        }
        assertNull(users.findUserByEmail("missing@test.com"))
    }

    @Test
    fun `updateUser creates the profile on first update and changes it later`() = runBlocking {
        val userId = users.createUser(email = "profile@test.com", password = "hash").id
        assertNull(users.findByUserProfileId(userId))

        users.updateUser(userId, UserUpdates(name = "Alex", age = 30))
        users.updateUser(userId, UserUpdates(name = null, age = 31))

        val profile = assertNotNull(users.findByUserProfileId(userId))
        assertEquals("Alex", profile.name)
        assertEquals(31, profile.age)
    }
}
