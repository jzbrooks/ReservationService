package com.jzbrooks.reservations.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time

@Serializable
data class Reservation(
    val name: String,
    val email: String,
    val partySize: Int,
    val date: String, // mm-dd-yyyy // todo: would be nice to improve this
    val time: String, // hh:mm
)

object Reservations : LongIdTable() {
    val name = varchar("name", 70)
    val email = varchar("email", 50)
    val partySize = integer("party_size")
    val date = date("date")
    val time = time("time")
    val inventoryId = long("fk_inventory_id")

    init {
        foreignKey(inventoryId to Inventory.id)
    }
}
