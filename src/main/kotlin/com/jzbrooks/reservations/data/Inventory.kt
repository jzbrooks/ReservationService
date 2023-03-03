package com.jzbrooks.reservations.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.time

@Serializable
data class InventoryDto(
    val startTime: String, // hh:mm
    val endTimeExclusive: String, // hh:mm
    val maxPartySize: Int,
    val maxReservations: Int,
)

object Inventory : Table() {
    val time = time("time")
    val maxPartySize = integer("max_party_size")
    val maxReservations = integer("max_reservations")

    override val primaryKey = PrimaryKey(time, name = "PK_Time")
}

