package com.jzbrooks.reservations.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchReplace
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

    override suspend fun createReservation(
        name: String,
        email: String,
        partySize: Int,
        date: LocalDate,
        time: LocalTime,
    ): Repository.CreateReservationResult {
        return newSuspendedTransaction {
            // todo: Is there a way to combine these two queries? Is Exposed going to make that hard?
            val inventoryForTime = Inventory
                .select { (Inventory.time eq time) and (Inventory.maxPartySize greaterEq partySize) }
                .singleOrNull() ?: return@newSuspendedTransaction Repository.CreateReservationResult.PARTY_TOO_LARGE

            val reservationsForInventory = Reservations.join(
                Inventory,
                JoinType.INNER,
                onColumn = Reservations.time,
                otherColumn = Inventory.time,
                additionalConstraint = {
                    (Reservations.time eq time) and
                        (Reservations.date eq date)
                },
            ).selectAll().count()

            if (reservationsForInventory < inventoryForTime[Inventory.maxReservations].toLong()) {
                Reservations.insert {
                    it[this.name] = name
                    it[this.email] = email
                    it[this.partySize] = partySize
                    it[this.date] = date
                    it[this.time] = time
                }
                Repository.CreateReservationResult.SUCCESS
            } else {
                Repository.CreateReservationResult.NO_INVENTORY
            }
        }
    }

    override suspend fun createInventory(times: Sequence<LocalTime>, maxPartySize: Int, maxReservations: Int) {
        newSuspendedTransaction {
            Inventory.batchReplace(times) {
                this[Inventory.time] = it
                this[Inventory.maxPartySize] = maxPartySize
                this[Inventory.maxReservations] = maxReservations
            }
        }
    }
}
