package com.jzbrooks.reservations

import com.jzbrooks.reservations.data.PostgresRepository
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(DefaultHeaders)
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.respondText(cause.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
            }
        }

        install(ContentNegotiation) {
            json()
        }

        // shortcut: Dagger for DI
        val repo = PostgresRepository()

        configureRouting(repo)
    }.start(wait = true)
}
