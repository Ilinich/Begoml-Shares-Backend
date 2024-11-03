package com.begoml.shares.app.di

import com.begoml.shares.data.database.DatabaseProvider
import com.begoml.shares.data.repository.encoder.EncoderRepositoryImpl
import com.begoml.shares.data.repository.token.TokenRepositoryImpl
import com.begoml.shares.data.repository.user.UserRepositoryImpl
import com.begoml.shares.logic.repositories.encoder.EncoderRepository
import com.begoml.shares.logic.repositories.token.TokenRepository
import com.begoml.shares.logic.repositories.user.UserRepository
import com.begoml.shares.logic.services.auth.AuthService
import com.begoml.shares.logic.services.user.UserService
import com.begoml.shares.presentation.features.auth.login.LoginController
import com.begoml.shares.presentation.features.auth.registration.RegistrationController
import com.begoml.shares.presentation.features.user.UserController
import mu.KLogger
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val moduleControllers = module {
    single { LoginController(authService = get()) }
    single { RegistrationController(authService = get()) }
    single { UserController(userService = get()) }
}

val moduleServices = module {
    single { UserService(userRepository = get()) }
    single {
        AuthService(
            userRepository = get(),
            tokenRepository = get(),
            encoderRepository = get(),
        )
    }
}

val moduleRepositories = module {
    single<UserRepository> {
        UserRepositoryImpl(
            databaseProvider = get(),
        )
    }

    single<TokenRepository> { TokenRepositoryImpl() }
    single<EncoderRepository> { EncoderRepositoryImpl() }
}

val moduleTools = module {
    single<KLogger> { KotlinLogging.logger {} }
}

val moduleDatabase = module {
    single { provideDatabaseProvider() }
}

fun provideDatabaseProvider(): DatabaseProvider {
    val shard1 = Database.connect(
        url = "jdbc:postgresql://localhost:5432/begomlshares",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "Postgres383",
    )

    val shard2 = Database.connect(
        url = "jdbc:postgresql://localhost:5432/begomlshares",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "Postgres383",
    )

    val shards = listOf(shard1, shard2)

    return DatabaseProvider(shards)
}