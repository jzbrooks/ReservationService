package com.jzbrooks.reservations.data

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.time
import java.time.LocalTime

data class InventoryEntry(
    val time: LocalTime,
    val maxPartySize: UInt,
    val maxReservations: UInt,
)

object Inventory : LongIdTable() {
    val time = time("time")
    val maxPartySize = uinteger("max_party_size")
    val maxReservations = uinteger("max_reservations")
}

