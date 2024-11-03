package com.begoml.shares.app

import com.begoml.shares.app.di.*
import com.begoml.shares.app.plugins.authentication.authentication
import com.begoml.shares.app.plugins.routing.configureRouting
import com.begoml.shares.app.plugins.serialization.configureSerialization
import com.begoml.shares.app.plugins.statuspages.statusPages
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(
        factory = CIO,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {

    install(Koin) {
        modules(moduleControllers)
        modules(moduleServices)
        modules(moduleRepositories)
        modules(moduleTools)
        modules(moduleDatabase)
    }

    authentication()
    configureRouting()
    statusPages()
    configureSerialization()
}