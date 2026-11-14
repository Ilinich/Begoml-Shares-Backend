package com.begoml.shares.logic

import com.begoml.shares.logic.exceptions.CommonException
import com.begoml.shares.logic.repositories.wallet.UserWallet
import com.begoml.shares.logic.repositories.wallet.UserWalletRateLimiterRepository
import com.begoml.shares.logic.repositories.wallet.UserWalletRepository
import com.begoml.shares.logic.repositories.wallet.WalletStatus
import com.begoml.shares.logic.services.wallet.UserWalletService
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.*

class UserWalletServiceTest {

    private class FakeWalletRepository(var status: WalletStatus = WalletStatus.ACTIVE) : UserWalletRepository {
        var balance: BigDecimal = BigDecimal("100")

        override suspend fun findWalletByUserId(userId: String) =
            UserWallet("w", userId, "main", "USD", balance, status, LocalDateTime.now())

        override suspend fun createWallet(userId: String, name: String) = Unit
        override suspend fun updateName(userId: String, newWalletName: String) = Unit
        override suspend fun updateStatus(userId: String, walletStatus: WalletStatus) = Unit

        override suspend fun deposit(userId: String, amount: BigDecimal): Boolean {
            balance += amount
            return true
        }

        override suspend fun withdraw(userId: String, amount: BigDecimal): Boolean {
            if (balance < amount) return false
            balance -= amount
            return true
        }
    }

    private val noRateLimit = object : UserWalletRateLimiterRepository {
        override suspend fun checkRateLimit(userId: String) = Unit
    }

    private fun statusOf(block: suspend () -> Unit): HttpStatusCode =
        assertFailsWith<CommonException> { runBlocking { block() } }.status

    @Test
    fun `non-positive amounts are rejected`() {
        val repo = FakeWalletRepository()
        val service = UserWalletService(repo, noRateLimit)

        for (amount in listOf("-1000", "0", "-0.00000001")) {
            assertEquals(HttpStatusCode.BadRequest, statusOf { service.withdraw("u", BigDecimal(amount)) })
            assertEquals(HttpStatusCode.BadRequest, statusOf { service.deposit("u", BigDecimal(amount)) })
        }
        assertEquals(BigDecimal("100"), repo.balance)
    }

    @Test
    fun `amounts with more than 8 decimals are rejected`() {
        val service = UserWalletService(FakeWalletRepository(), noRateLimit)

        assertEquals(HttpStatusCode.BadRequest, statusOf { service.deposit("u", BigDecimal("0.000000001")) })
    }

    @Test
    fun `insufficient balance is reported as forbidden`() {
        val service = UserWalletService(FakeWalletRepository(), noRateLimit)

        assertEquals(HttpStatusCode.Forbidden, statusOf { service.withdraw("u", BigDecimal("100.01")) })
    }

    @Test
    fun `suspended wallet is rejected`() {
        val service = UserWalletService(FakeWalletRepository(status = WalletStatus.SUSPENDED), noRateLimit)

        assertEquals(HttpStatusCode.Forbidden, statusOf { service.deposit("u", BigDecimal("1")) })
    }
}
