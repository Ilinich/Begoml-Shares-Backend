package com.begoml.shares.data

import com.begoml.shares.data.repository.user.UserRepositoryImpl
import com.begoml.shares.data.repository.wallet.UserWalletRepositoryImpl
import com.begoml.shares.logic.repositories.wallet.WalletStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import kotlin.test.*

class UserWalletRepositoryImplTest {

    private val databaseProvider = testDatabaseProvider()
    private val users = UserRepositoryImpl(databaseProvider)
    private val wallets = UserWalletRepositoryImpl(databaseProvider)

    private fun newWallet(balance: BigDecimal = BigDecimal.ZERO): String = runBlocking {
        val userId = users.createUser(email = "${System.nanoTime()}@test.com", password = "hash").id
        wallets.createWallet(userId = userId, name = "main")
        if (balance.signum() > 0) assertTrue(wallets.deposit(userId, balance))
        userId
    }

    private fun balanceOf(userId: String) = runBlocking { wallets.findWalletByUserId(userId)!!.balances }

    @Test
    fun `deposit and withdraw change the balance`() = runBlocking {
        val userId = newWallet()

        assertTrue(wallets.deposit(userId, BigDecimal("100")))
        assertTrue(wallets.withdraw(userId, BigDecimal("30.5")))

        assertEquals(0, BigDecimal("69.5").compareTo(balanceOf(userId)))
    }

    @Test
    fun `withdraw more than the balance is rejected and keeps the balance`() = runBlocking {
        val userId = newWallet(BigDecimal("10"))

        assertFalse(wallets.withdraw(userId, BigDecimal("10.01")))

        assertEquals(0, BigDecimal("10").compareTo(balanceOf(userId)))
    }

    @Test
    fun `inactive wallet cannot be credited or debited`() = runBlocking {
        val userId = newWallet(BigDecimal("10"))
        wallets.updateStatus(userId, WalletStatus.SUSPENDED)

        assertFalse(wallets.deposit(userId, BigDecimal("1")))
        assertFalse(wallets.withdraw(userId, BigDecimal("1")))
        assertEquals(0, BigDecimal("10").compareTo(balanceOf(userId)))
    }

    @Test
    fun `concurrent withdrawals never overdraw`() = runBlocking {
        val userId = newWallet(BigDecimal("100"))

        val results = (1..50).map {
            async(Dispatchers.IO) { wallets.withdraw(userId, BigDecimal("10")) }
        }.awaitAll()

        assertEquals(10, results.count { it })
        assertEquals(0, BigDecimal.ZERO.compareTo(balanceOf(userId)))
    }

    @Test
    fun `wallets get their own creation time`() = runBlocking {
        val first = newWallet()
        Thread.sleep(20)
        val second = newWallet()

        assertNotEquals(
            wallets.findWalletByUserId(first)!!.createdAt,
            wallets.findWalletByUserId(second)!!.createdAt,
        )
    }
}
