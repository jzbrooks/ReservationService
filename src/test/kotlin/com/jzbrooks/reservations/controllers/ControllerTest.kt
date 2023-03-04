package com.jzbrooks.reservations.controllers

import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.jzbrooks.reservations.data.InventoryDto
import com.jzbrooks.reservations.data.Repository
import com.jzbrooks.reservations.data.ReservationDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ControllerTest {
    val noOpRepository = object : Repository {
        override suspend fun createReservation(
            name: String,
            email: String,
            partySize: Int,
            date: LocalDate,
            time: LocalTime,
        ): Repository.CreateReservationResult {
            return Repository.CreateReservationResult.SUCCESS
        }

        override suspend fun createInventory(
            times: Sequence<LocalTime>,
            maxPartySize: Int,
            maxReservations: Int,
        ) { }
    }

    val reservation = ReservationDto(
        "Justin Brooks",
        "justin@example.com",
        8,
        "12-01-2023",
        "12:00"
    )

    val inventory = InventoryDto(
        "12:00",
        "19:00",
        8,
        30,
    )

    @Test
    fun `create reservation success`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createReservation(reservation)
        assertThat(result).isInstanceOf(ControllerResult.Success::class)
    }

    @Test
    fun `create reservation invalid name`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createReservation(reservation.copy(name = ""))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }

    @Test
    fun `create reservation invalid email`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createReservation(reservation.copy(email = "test@example.a"))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }

    @Test
    fun `create reservation invalid party size`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createReservation(reservation.copy(partySize = -1))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }

    @Test
    fun `create reservation invalid date`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createReservation(reservation.copy(date = "1203-01-32"))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }

    @Test
    fun `create reservation invalid time`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createReservation(reservation.copy(time = "30:30"))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }

    @Test
    fun `create inventory success`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createInventory(inventory)
        assertThat(result).isInstanceOf(ControllerResult.Success::class)
    }

    @Test
    fun `create inventory invalid start time`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createInventory(inventory.copy(startTime = "30:30"))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }

    @Test
    fun `create inventory invalid end time`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createInventory(inventory.copy(endTimeExclusive = "30:30"))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }

    @Test
    fun `create inventory invalid max party size`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createInventory(inventory.copy(maxPartySize = -1))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }

    @Test
    fun `create inventory invalid max reservations`() = runTest {
        val controller = Controller(noOpRepository)
        val result = controller.createInventory(inventory.copy(maxReservations = -1))
        assertThat(result).isInstanceOf(ControllerResult.BadRequest::class)
    }
}
