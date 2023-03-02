package com.jzbrooks.reservations

import com.jzbrooks.reservations.data.Repository
import com.jzbrooks.reservations.data.Reservation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

fun Application.configureRouting(repository: Repository) {
    routing {
        get("/") {
            call.respondText("Registration Service v1")
        }

        // Assumption: there is only one timezone.
        post("reservations") {
            val reservation = call.receive<Reservation>()

            if (!reservation.date.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}".toRegex())) {
                call.respond(HttpStatusCode.BadRequest, "Invalid date (mm-dd-yyyy).")
                return@post
            }

            val (month, day, year) = reservation.date.split('-').map(String::toInt)
            val date = LocalDate.of(year, month, day)

            if (!reservation.time.matches("[0-9]{2}:[0-9]{2}".toRegex())) {
                call.respond(HttpStatusCode.BadRequest, "Invalid time (hh:mm).")
                return@post
            }

            val (hour, minutes) = reservation.time.split(':').map(String::toInt)
            if (minutes % 15 != 0) {
                call.respond(HttpStatusCode.BadRequest, "Reservations can only be placed on quarter hour intervals.")
                return@post
            }

            val time = LocalTime.of(hour, minutes)

            val result = repository.createReservation(
                reservation.name,
                reservation.email,
                reservation.partySize,
                date,
                time
            )

            when (result) {
                Repository.CreateReservationResult.SUCCESS -> call.respond(HttpStatusCode.NoContent)
                Repository.CreateReservationResult.PARTY_TOO_LARGE -> call.respond(HttpStatusCode.BadRequest, "The party is too large.")
                Repository.CreateReservationResult.NO_INVENTORY -> call.respond(HttpStatusCode.BadRequest, "No inventory is available for ${reservation.date} at ${reservation.time}.")
            }
        }
    }
}
