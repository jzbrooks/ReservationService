package com.jzbrooks.reservations.data

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time

@Serializable
data class ReservationDto(
    val name: String,
    val email: String,
    val partySize: Int,
    val date: String, // mm-dd-yyyy // todo: would be nice to improve this
    val time: String, // hh:mm
)

object Reservations : Table() {
    val email = varchar("email", 50)
    val date = date("date").index()
    val time = time("time").index()
    val partySize = integer("party_size")
    val name = varchar("name", 70)

    override val primaryKey = PrimaryKey(email, date, time, name = "PK_email_date_time")
}
