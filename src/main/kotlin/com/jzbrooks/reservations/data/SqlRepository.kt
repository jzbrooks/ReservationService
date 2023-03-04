package com.jzbrooks.reservations.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.batchReplace
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalTime

class SqlRepository(driver: String, connection: String) : Repository {

    init {
        val config = HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = connection
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
            val inventoryForTime = Inventory
                .select { (Inventory.time eq time) and (Inventory.maxPartySize greaterEq partySize) }
                .singleOrNull() ?: return@newSuspendedTransaction Repository.CreateReservationResult.NO_INVENTORY_FOR_PARTY

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
                try {
                    Reservations.insert {
                        it[this.name] = name
                        it[this.email] = email
                        it[this.partySize] = partySize
                        it[this.date] = date
                        it[this.time] = time
                    }
                    Repository.CreateReservationResult.SUCCESS
                } catch (e: SQLException) {
                    Repository.CreateReservationResult.CONSTRAINT_VIOLATED
                }
            } else {
                Repository.CreateReservationResult.INVENTORY_AT_CAPACITY
            }
        }
    }

    override suspend fun createInventory(times: Sequence<LocalTime>, maxPartySize: Int, maxReservations: Int) {
        newSuspendedTransaction {
            Inventory.batchInsert(times) {
                this[Inventory.time] = it
                this[Inventory.maxPartySize] = maxPartySize
                this[Inventory.maxReservations] = maxReservations
            }
        }
    }

    override suspend fun updateInventory(
        beginning: LocalDate,
        times: Sequence<LocalTime>,
        maxPartySize: Int,
        maxReservations: Int,
    ): Repository.UpdateInventoryResult {
        return newSuspendedTransaction {
            val reservationsAfterDate = Reservations.select {
                Reservations.date greaterEq beginning
            }.count()

            if (reservationsAfterDate == 0L) {
                Inventory.batchReplace(times) {
                    this[Inventory.time] = it
                    this[Inventory.maxPartySize] = maxPartySize
                    this[Inventory.maxReservations] = maxReservations
                }

                Repository.UpdateInventoryResult.SUCCESS
            } else {
                Repository.UpdateInventoryResult.FAILURE_RESERVATIONS_EXIST
            }
        }
    }
}
