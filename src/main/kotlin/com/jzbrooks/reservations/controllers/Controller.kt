package com.jzbrooks.reservations.controllers

import com.jzbrooks.reservations.data.InventoryDto
import com.jzbrooks.reservations.data.Repository
import com.jzbrooks.reservations.data.ReservationDto
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime

class Controller(private val repository: Repository) {

    suspend fun createReservation(reservationDto: ReservationDto): ControllerResult<Any?> {
        if (reservationDto.name.isEmpty()) {
            return ControllerResult.BadRequest("Name must be specified.")
        }

        if (!validEmail.matches(reservationDto.email)) {
            return ControllerResult.BadRequest("A valid email is required.")
        }

        if (reservationDto.partySize < 1) {
            return ControllerResult.BadRequest("Party must be at least one person")
        }

        if (!validDate.matches(reservationDto.date)) {
            return ControllerResult.BadRequest("Invalid date - The required format is (mm-dd-yyyy).")
        }

        val (month, day, year) = reservationDto.date.split('-').map(String::toInt)
        val date = try {
            LocalDate.of(year, month, day)
        } catch (e: DateTimeException) {
            return ControllerResult.BadRequest("Invalid date $e")
        }

        if (!validTime.matches(reservationDto.time)) {
            return ControllerResult.BadRequest("Invalid time - The required format is (hh:mm)")
        }

        val (hour, minutes) = reservationDto.time.split(':').map(String::toInt)
        if (minutes % 15 != 0) {
            return ControllerResult.BadRequest("Reservations can only be placed on quarter hour intervals.")
        }

        val time = try {
            LocalTime.of(hour, minutes)
        } catch (e: DateTimeException) {
            return ControllerResult.BadRequest("Invalid time $e")
        }

        val result = repository.createReservation(
            reservationDto.name,
            reservationDto.email,
            reservationDto.partySize,
            date,
            time
        )

        return when (result) {
            Repository.CreateReservationResult.SUCCESS -> ControllerResult.Success(null)
            Repository.CreateReservationResult.PARTY_TOO_LARGE -> ControllerResult.BadRequest("The party is too large.")
            Repository.CreateReservationResult.NO_INVENTORY -> ControllerResult.BadRequest("No inventory is available for ${reservationDto.date} at ${reservationDto.time}.")
            Repository.CreateReservationResult.CONSTRAINT_VIOLATED -> ControllerResult.BadRequest("A reservation already exists for that time.")
        }
    }

    suspend fun createInventory(inventoryDto: InventoryDto): ControllerResult<Any?> {
        if (inventoryDto.maxPartySize < 1) {
            return ControllerResult.BadRequest("Maximum party size must at least be one")
        }

        if (inventoryDto.maxReservations < 1) {
            return ControllerResult.BadRequest("Maximum party size must at least be one")
        }

        if (!validTime.matches(inventoryDto.startTime)) {
            return ControllerResult.BadRequest("Invalid time - The required format is (hh:mm)")
        }

        val (startHour, startMinutes) = inventoryDto.startTime.split(':').map(String::toInt)
        if (startMinutes % 15 != 0) {
            return ControllerResult.BadRequest("Inventory can only be created for quarter hour intervals.")
        }

        val startTime = try {
            LocalTime.of(startHour, startMinutes)
        } catch (e: DateTimeException) {
            return ControllerResult.BadRequest("Invalid time $e")
        }

        if (!validTime.matches(inventoryDto.endTimeExclusive)) {
            return ControllerResult.BadRequest("Invalid time - The required format is (hh:mm)")
        }

        val (endHour, endMinutes) = inventoryDto.endTimeExclusive.split(':').map(String::toInt)
        if (endMinutes % 15 != 0) {
            return ControllerResult.BadRequest("Inventory can only be created for quarter hour intervals.")
        }

        val endTime = try {
            LocalTime.of(endHour, endMinutes)
        } catch (e: DateTimeException) {
            return ControllerResult.BadRequest("Invalid time $e")
        }

        val times = generateSequence(startTime) {
            it.plusMinutes(15)
        }.takeWhile { it < endTime }

        repository.createInventory(times, inventoryDto.maxPartySize, inventoryDto.maxReservations)

        return ControllerResult.Success(null)
    }

    private companion object {
        // This isn't a great email pattern, and regex validation
        // for email is notoriously imperfect with a reasonable regular expression.
        // So this is the simple path that provides some imperfect validation
        val validEmail = Regex(".+@(.+\\.)+.{2,24}")
        val validTime = Regex("[0-9]{2}:[0-9]{2}")
        val validDate = Regex("[0-9]{2}-[0-9]{2}-[0-9]{4}")
    }
}
