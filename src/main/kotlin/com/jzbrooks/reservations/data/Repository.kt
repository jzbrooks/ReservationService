package com.jzbrooks.reservations.data

import java.time.LocalDate
import java.time.LocalTime

interface Repository {
    suspend fun getReservations(): List<ReservationDto>

    suspend fun createReservation(
        name: String,
        email: String,
        partySize: Int,
        date: LocalDate,
        time: LocalTime,
    ): CreateReservationResult

    enum class CreateReservationResult {
        SUCCESS,
        NO_INVENTORY_FOR_PARTY,
        INVENTORY_AT_CAPACITY,
        CONSTRAINT_VIOLATED,
    }

    suspend fun getInventory(): List<InventoryDto.Get>

    suspend fun createInventory(
        times: Sequence<LocalTime>,
        maxPartySize: Int,
        maxReservations: Int,
    )

    suspend fun updateInventory(
        beginning: LocalDate,
        times: Sequence<LocalTime>,
        maxPartySize: Int,
        maxReservations: Int,
    ): UpdateInventoryResult

    enum class UpdateInventoryResult {
        SUCCESS,
        FAILURE_RESERVATIONS_EXIST,
    }
}
