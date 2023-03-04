package com.jzbrooks.reservations.data

import java.time.LocalDate
import java.time.LocalTime

interface Repository {
    suspend fun createReservation(
        name: String,
        email: String,
        partySize: Int,
        date: LocalDate,
        time: LocalTime,
    ): CreateReservationResult

    enum class CreateReservationResult {
        SUCCESS,
        PARTY_TOO_LARGE,
        NO_INVENTORY,
        CONSTRAINT_VIOLATED,
    }

    suspend fun createInventory(
        times: Sequence<LocalTime>,
        maxPartySize: Int,
        maxReservations: Int,
    )
}
