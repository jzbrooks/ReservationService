package com.jzbrooks.reservations.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SqlRepositoryTest {
    val repo = SqlRepository("org.h2.Driver", "jdbc:h2:mem:test")

    @BeforeEach
    fun setup() {
        transaction {
            Reservations.deleteAll()
            Inventory.deleteAll()
        }
    }

    @Test
    fun `insert inventory for single time`() = runTest {
        repo.createInventory(sequenceOf(LocalTime.of(12, 0)), 8, 10)

        transaction {
            assertThat(Inventory.selectAll().count()).isEqualTo(1)
        }
    }

    @Test
    fun `insert inventory for range`() = runTest {
        val times = generateSequence(LocalTime.of(12, 0)) {
            it.plusMinutes(15)
        }.take(10)

        repo.createInventory(times, 8, 10)

        transaction {
            assertThat(Inventory.selectAll().count()).isEqualTo(10)
        }
    }

    @Test
    fun `overlapping inventory is modified`() = runTest {
        val times = generateSequence(LocalTime.of(12, 0)) {
            it.plusMinutes(15)
        }.take(10)

        repo.createInventory(times, 8, 10)

        val updatedTimes = generateSequence(LocalTime.of(13, 0)) {
            it.plusMinutes(15)
        }.take(4)

        repo.createInventory(updatedTimes, 2, 10)

        transaction {
            assertThat(Inventory.select { Inventory.maxPartySize eq 2 }.count()).isEqualTo(4)
        }

        transaction {
            assertThat(Inventory.select { Inventory.maxPartySize eq 8 }.count()).isEqualTo(6)
        }
    }

    @Test
    fun `insert repository for valid inventory`() = runTest {
        repo.createInventory(sequenceOf(LocalTime.of(12, 0)), 8, 10)

        repo.createReservation(
            "Justin Brooks",
            "justin@example.com",
            8,
            LocalDate.of(2024, 10, 30),
            LocalTime.of(12, 0),
        )

        transaction {
            assertThat(Reservations.selectAll().count()).isEqualTo(1)
        }
    }

    @Test
    fun `reservation not inserted for party too large`() = runTest {
        repo.createInventory(sequenceOf(LocalTime.of(12, 0)), 8, 10)

        repo.createReservation(
            "Justin Brooks",
            "justin@example.com",
            10,
            LocalDate.of(2024, 10, 30),
            LocalTime.of(12, 0),
        )

        transaction {
            assertThat(Reservations.selectAll().count()).isEqualTo(0)
        }
    }

    @Test
    fun `reservation not inserted without corresponding inventory`() = runTest {
        repo.createInventory(sequenceOf(LocalTime.of(12, 0)), 8, 10)

        repo.createReservation(
            "Justin Brooks",
            "justin@example.com",
            8,
            LocalDate.of(2024, 10, 30),
            LocalTime.of(13, 0),
        )

        transaction {
            assertThat(Reservations.selectAll().count()).isEqualTo(0)
        }
    }

    @Test
    fun `duplicate reservation not inserted`() = runTest {
        repo.createInventory(sequenceOf(LocalTime.of(12, 0)), 8, 10)
        repo.createReservation(
            "Justin Brooks",
            "justin@example.com",
            8,
            LocalDate.of(2024, 10, 30),
            LocalTime.of(12, 0),
        )

        val result = repo.createReservation(
            "Justin Brooks",
            "justin@example.com",
            8,
            LocalDate.of(2024, 10, 30),
            LocalTime.of(12, 0),
        )

        assertThat(result).isEqualTo(Repository.CreateReservationResult.CONSTRAINT_VIOLATED)
    }
}