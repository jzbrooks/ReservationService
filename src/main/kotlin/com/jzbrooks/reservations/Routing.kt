package com.jzbrooks.reservations

import com.jzbrooks.reservations.controllers.Controller
import com.jzbrooks.reservations.controllers.ControllerResult
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

fun Application.configureRouting(controller: Controller) {
    routing {
        get("/") {
            call.respondText("Registration Service v1")
        }

        // Assumption: there is only one timezone.
        post("reservations") {
            val reservationDto = call.receive<ReservationDto>()
            when (val result = controller.createReservation(reservationDto)) {
                is ControllerResult.Success -> call.respond(HttpStatusCode.Created)
                is ControllerResult.BadRequest -> call.respond(HttpStatusCode.BadRequest, result.message)
                is ControllerResult.NotFound -> call.respond(HttpStatusCode.NotFound, result.message)
            }
        }

        post("inventory") {
            val inventoryDto = call.receive<InventoryDto.Create>()
            when (val result = controller.createInventory(inventoryDto)) {
                is ControllerResult.Success -> call.respond(HttpStatusCode.Created)
                is ControllerResult.BadRequest -> call.respond(HttpStatusCode.BadRequest, result.message)
                is ControllerResult.NotFound -> call.respond(HttpStatusCode.NotFound, result.message)
            }
        }

        put("inventory/update") {
            val inventoryDto = call.receive<InventoryDto.UpdateAfterDate>()
            when (val result = controller.updateInventoryAfterDate(inventoryDto)) {
                is ControllerResult.Success -> call.respond(HttpStatusCode.NoContent)
                is ControllerResult.BadRequest -> call.respond(HttpStatusCode.BadRequest, result.message)
                is ControllerResult.NotFound -> call.respond(HttpStatusCode.NotFound, result.message)
            }
        }
    }
}
