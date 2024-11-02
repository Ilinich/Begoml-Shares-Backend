package com.begoml.shares.app

import com.begoml.shares.app.di.moduleControllers
import com.begoml.shares.app.di.moduleRepositories
import com.begoml.shares.app.di.moduleServices
import com.begoml.shares.app.di.moduleTools
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
    }

    authentication()
    configureRouting()
    statusPages()
    configureSerialization()
}