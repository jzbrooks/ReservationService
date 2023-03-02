package com.jzbrooks.reservations.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalTime

class PostgresRepository : Repository {

    init {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = System.getenv("JDBC_DATABASE_URL")
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(config))

        transaction {
            SchemaUtils.create(Reservations, Inventory)
        }
    }

    override fun createReservation(
        name: String,
        email: String,
        partySize: Int,
        date: LocalDate,
        time: LocalTime,
    ): Repository.CreateReservationResult {
        return transaction {
            // todo: Is there a way to combine these two queries? Is Exposed going to make that hard?
            val inventoryForTime = Inventory
                .select { (Inventory.time eq time) and (Inventory.maxPartySize greaterEq partySize) }
                .singleOrNull() ?: return@transaction Repository.CreateReservationResult.PARTY_TOO_LARGE

            val reservationsForInventory = Reservations.join(
                Inventory,
                JoinType.INNER,
                onColumn = Reservations.inventoryId,
                otherColumn = Inventory.id,
                additionalConstraint = {
                    (Reservations.time eq time) and
                        (Reservations.date eq date)
                },
            )
                .selectAll()
                .toList() // assumption: the result set is small

            if (reservationsForInventory.size < inventoryForTime[Inventory.maxReservations].toLong()) {
                Reservations.insert {
                    it[this.name] = name
                    it[this.email] = email
                    it[this.partySize] = partySize
                    it[this.date] = date
                    it[this.time] = time
                    it[this.inventoryId] = inventoryForTime[Inventory.id].value
                }
                Repository.CreateReservationResult.SUCCESS
            } else {
                Repository.CreateReservationResult.NO_INVENTORY
            }
        }
    }
}
