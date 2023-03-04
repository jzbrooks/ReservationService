package com.jzbrooks.reservations.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.time

sealed interface InventoryDto {
    val startTime: String // hh:mm
    val endTimeExclusive: String // hh:mm
    val maxPartySize: Int
    val maxReservations: Int

    @Serializable
    data class Create(
        override val startTime: String, // hh:mm
        override val endTimeExclusive: String, // hh:mm
        override val maxPartySize: Int,
        override val maxReservations: Int,
    ) : InventoryDto

    @Serializable
    data class UpdateAfterDate(
        val startDate: String, // mm-dd-yyyy
        override val startTime: String, // hh:mm
        override val endTimeExclusive: String, // hh:mm
        override val maxPartySize: Int,
        override val maxReservations: Int,
    ) : InventoryDto
}


object Inventory : Table() {
    val time = time("time")
    val maxPartySize = integer("max_party_size")
    val maxReservations = integer("max_reservations")

    override val primaryKey = PrimaryKey(time, name = "PK_Time")
}

