package com.jzbrooks.reservations.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.time

sealed interface InventoryDto {
    val maxPartySize: Int
    val maxReservations: Int

    @Serializable
    data class Get(
        val time: String, // hh:mm
        override val maxPartySize: Int,
        override val maxReservations: Int,
    ) : InventoryDto

    interface Write : InventoryDto {
        val startTime: String // hh:mm
        val endTimeExclusive: String // hh:mm
    }

    @Serializable
    data class Create(
        override val startTime: String, // hh:mm
        override val endTimeExclusive: String, // hh:mm
        override val maxPartySize: Int,
        override val maxReservations: Int,
    ) : InventoryDto.Write

    @Serializable
    data class UpdateAfterDate(
        val startDate: String, // mm-dd-yyyy
        override val startTime: String, // hh:mm
        override val endTimeExclusive: String, // hh:mm
        override val maxPartySize: Int,
        override val maxReservations: Int,
    ) : InventoryDto.Write
}

object Inventory : Table() {
    val time = time("time")
    val maxPartySize = integer("max_party_size")
    val maxReservations = integer("max_reservations")

    override val primaryKey = PrimaryKey(time, name = "PK_Time")
}

