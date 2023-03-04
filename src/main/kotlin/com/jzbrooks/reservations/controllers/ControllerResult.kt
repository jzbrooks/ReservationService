package com.jzbrooks.reservations.controllers

sealed interface ControllerResult<T> {
    data class Success<T>(val data: T) : ControllerResult<T>

    sealed interface Error<T> : ControllerResult<T> {
        val message: String
    }

    data class BadRequest<T>(override val message: String) : Error<T>
    data class NotFound<T>(override val message: String) : Error<T>
}
