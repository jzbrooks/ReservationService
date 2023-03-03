package com.jzbrooks.reservations

import com.jzbrooks.reservations.data.InventoryDto
import com.jzbrooks.reservations.data.Repository
import com.jzbrooks.reservations.data.ReservationDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.LocalTime

fun Application.configureRouting(repository: Repository) {
    routing {
        get("/") {
            call.respondText("Registration Service v1")
        }

        // Assumption: there is only one timezone.
        post("reservations") {
            val reservationDto = call.receive<ReservationDto>()

            // todo: validate partySize is [0, ∞), name is not empty, email is valid (for common cases)

            if (!reservationDto.date.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}".toRegex())) {
                call.respond(HttpStatusCode.BadRequest, "Invalid date (mm-dd-yyyy).")
                return@post
            }

            val (month, day, year) = reservationDto.date.split('-').map(String::toInt)
            val date = LocalDate.of(year, month, day)

            if (!reservationDto.time.matches("[0-9]{2}:[0-9]{2}".toRegex())) {
                call.respond(HttpStatusCode.BadRequest, "Invalid time (hh:mm).")
                return@post
            }

            val (hour, minutes) = reservationDto.time.split(':').map(String::toInt)
            if (minutes % 15 != 0) {
                call.respond(HttpStatusCode.BadRequest, "Reservations can only be placed on quarter hour intervals.")
                return@post
            }

            val time = LocalTime.of(hour, minutes)

            val result = repository.createReservation(
                reservationDto.name,
                reservationDto.email,
                reservationDto.partySize,
                date,
                time
            )

            when (result) {
                Repository.CreateReservationResult.SUCCESS -> call.respond(HttpStatusCode.Created)
                Repository.CreateReservationResult.PARTY_TOO_LARGE -> call.respond(HttpStatusCode.BadRequest, "The party is too large.")
                Repository.CreateReservationResult.NO_INVENTORY -> call.respond(HttpStatusCode.BadRequest, "No inventory is available for ${reservationDto.date} at ${reservationDto.time}.")
            }
        }

        put("inventory") {
            val inventoryDto = call.receive<InventoryDto>()

            // TODO: factor out validation for DRY-ness (and probably a 'controller' for testing)
            if (!inventoryDto.startTime.matches("[0-9]{2}:[0-9]{2}".toRegex())) {
                call.respond(HttpStatusCode.BadRequest, "Invalid time (hh:mm).")
                return@put
            }

            val (startHour, startMinutes) = inventoryDto.startTime.split(':').map(String::toInt)
            if (startMinutes % 15 != 0) {
                call.respond(HttpStatusCode.BadRequest, "Inventory can only be created for quarter hour intervals.")
                return@put
            }

            val startTime = LocalTime.of(startHour, startMinutes)

            // TODO: factor out validation for DRY-ness (and probably a 'controller' for testing)
            if (!inventoryDto.endTimeExclusive.matches("[0-9]{2}:[0-9]{2}".toRegex())) {
                call.respond(HttpStatusCode.BadRequest, "Invalid time (hh:mm).")
                return@put
            }

            val (endHour, endMinutes) = inventoryDto.endTimeExclusive.split(':').map(String::toInt)
            if (endMinutes % 15 != 0) {
                call.respond(HttpStatusCode.BadRequest, "Inventory can only be created for quarter hour intervals.")
                return@put
            }

            val endTime = LocalTime.of(endHour, endMinutes)

            val times = generateSequence(startTime) {
                it.plusMinutes(15)
            }.takeWhile { it < endTime }

            repository.createInventory(times, inventoryDto.maxPartySize, inventoryDto.maxReservations)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
