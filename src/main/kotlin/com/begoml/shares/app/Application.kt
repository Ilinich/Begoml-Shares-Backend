package com.begoml.shares.app

import com.begoml.shares.app.di.*
import com.begoml.shares.app.plugins.authentication.authentication
import com.begoml.shares.app.plugins.routing.configureRouting
import com.begoml.shares.app.plugins.serialization.configureSerialization
import com.begoml.shares.app.plugins.sockets.configureSockets
import com.begoml.shares.app.plugins.statuspages.statusPages
import com.begoml.shares.core.AppQuartzScheduler
import com.begoml.shares.presentation.services.RabbitMQService
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.dsl.module

fun main() {
    embeddedServer(
        factory = CIO,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    // Read eagerly so a missing secret stops the server at startup, not on the first request.
    val appConfig = AppConfig.fromEnv()

    install(Koin) {
        modules(module { single { appConfig } })
        modules(moduleControllers)

        modules(moduleFeatureServices)
        modules(moduleCore)

        modules(moduleAppWebSocket)
        modules(moduleAppEvents)

        modules(moduleServices)
        modules(moduleRepositories)
        modules(moduleDatabase)

        modules(moduleTools)
        modules(rabbitModule)
        modules(loggerModule)
    }

    authentication()
    configureRouting()
    statusPages()
    configureSerialization()

    startRabbitMQService()
    startServices()

    configureSockets()
}

fun Application.startServices() {
    val appQuartzScheduler by inject<AppQuartzScheduler>()
    appQuartzScheduler.start()
    environment.monitor.subscribe(ApplicationStopping) { appQuartzScheduler.stop() }
}

fun Application.startRabbitMQService() {
    val rabbitMQService by inject<RabbitMQService>()
    rabbitMQService.start()
    environment.monitor.subscribe(ApplicationStopping) { rabbitMQService.stop() }
}
