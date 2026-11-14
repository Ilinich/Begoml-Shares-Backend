package com.begoml.shares.app.di

import com.begoml.shares.app.*
import com.begoml.shares.core.AppQuartzScheduler
import com.begoml.shares.core.AppQuartzSchedulerImpl
import com.begoml.shares.data.database.DatabaseProvider
import com.begoml.shares.data.tools.JwtConfig
import com.begoml.shares.data.repository.encoder.EncoderRepositoryImpl
import com.begoml.shares.data.repository.shares.SharesRepositoryImpl
import com.begoml.shares.data.repository.token.TokenRepositoryImpl
import com.begoml.shares.data.repository.user.UserRepositoryImpl
import com.begoml.shares.data.repository.wallet.UserWalletRateLimiterRepositoryImpl
import com.begoml.shares.data.repository.wallet.UserWalletRepositoryImpl
import com.begoml.shares.logic.repositories.encoder.EncoderRepository
import com.begoml.shares.logic.repositories.shares.SharesRepository
import com.begoml.shares.logic.repositories.token.TokenRepository
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.repositories.wallet.UserWalletRateLimiterRepository
import com.begoml.shares.logic.repositories.wallet.UserWalletRepository
import com.begoml.shares.logic.services.auth.AuthService
import com.begoml.shares.logic.services.confirmation.ConfirmationCodeService
import com.begoml.shares.logic.services.user.UserService
import com.begoml.shares.logic.services.wallet.UserWalletService
import com.begoml.shares.presentation.features.auth.login.LoginController
import com.begoml.shares.presentation.features.auth.registration.RegistrationController
import com.begoml.shares.presentation.features.events.SharePriceNotifier
import com.begoml.shares.presentation.features.events.WebSocketManager
import com.begoml.shares.presentation.features.shares.SharesController
import com.begoml.shares.presentation.features.user.UserController
import com.begoml.shares.presentation.features.wallet.UserWalletController
import com.begoml.shares.presentation.services.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.UnifiedJedis

val moduleControllers = module {
    single { LoginController(authService = get()) }
    single { RegistrationController(authService = get()) }
    single { UserController(userService = get()) }
    single { SharesController(sharesRepository = get()) }
    single { UserWalletController(walletService = get(), confirmationCodeService = get()) }
}

val moduleFeatureServices = module {
    single {
        SharesPriceUpdateService(
            scope = get(),
            logger = get(),
            sharesRepository = get(),
            rabbitEmitter = get(),
        )
    }
    single {
        SharesNewsUpdateService(
            scope = get(),
            logger = get(),
        )
    }
}

val moduleAppWebSocket = module {
    single {
        WebSocketManager(
            scope = get(),
            json = get(),
        )
    }
}

val moduleAppEvents = module {
    single {
        SharePriceNotifier(
            scope = get(),
            sharesRepository = get(),
            webSocketManager = get(),
            rabbitConsumer = get(),
        )
    }
}

val moduleCore = module {
    single<AppQuartzScheduler> {
        AppQuartzSchedulerImpl(
            sharesPriceService = get(),
            sharesNewsService = get(),
        )
    }
}

val moduleServices = module {
    single { UserService(userRepository = get()) }

    single { ConfirmationCodeService() }

    single {
        AuthService(
            userRepository = get(),
            tokenRepository = get(),
            encoderRepository = get(),
        )
    }

    single {
        UserWalletService(
            walletRepository = get(),
            rateLimiter = get(),
        )
    }
}

val moduleRepositories = module {
    single<UserRepository> { UserRepositoryImpl(databaseProvider = get()) }

    single<UserWalletRepository> { UserWalletRepositoryImpl(databaseProvider = get()) }
    single<UserWalletRateLimiterRepository> {
        UserWalletRateLimiterRepositoryImpl(redisClient = get())
    }

    single { JwtConfig(secret = get<AppConfig>().jwtSecret) }
    single<TokenRepository> { TokenRepositoryImpl(jwtConfig = get()) }
    single<EncoderRepository> { EncoderRepositoryImpl(legacySecret = get<AppConfig>().legacyPasswordSecret) }

    single<SharesRepository> {
        SharesRepositoryImpl(
            databaseProvider = get(),
            json = get(),
            redisClient = get(),
        )
    }
}

val moduleTools = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single<Json> {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}

val loggerModule = module {
    single<KLogger> { KotlinLogging.logger("BegomlSharesLogger") }
}

val moduleDatabase = module {
    single { provideDatabaseProvider(config = get()) }
    single<UnifiedJedis> { provideRedisClient(config = get()) }
}

val rabbitModule = module {
    single<RabbitMQService> {
        RabbitMQService(
            json = get(),
            logger = get(),
            host = get<AppConfig>().rabbitHost,
            port = get<AppConfig>().rabbitPort,
        )
    }
    single<RabbitEmitter> { get<RabbitMQService>() }
    single<RabbitConsumer> { get<RabbitMQService>() }
}

fun provideDatabaseProvider(config: AppConfig): DatabaseProvider {
    fun connect(url: String) = Database.connect(
        url = url,
        driver = "org.postgresql.Driver",
        user = config.databaseUser,
        password = config.databasePassword,
    )

    return DatabaseProvider(
        physicalShards = config.databaseShardUrls.map(::connect),
        shares = connect(config.databaseSharesUrl),
    )
}

// A single Jedis connection is not thread-safe; JedisPooled borrows a pooled connection per command.
fun provideRedisClient(config: AppConfig): UnifiedJedis = JedisPooled(config.redisHost, config.redisPort)
