package com.example.jetpackstayrooms.domain.usecase

import com.example.jetpackstayrooms.domain.BookingWithDetails
import com.example.jetpackstayrooms.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Devuelve, de forma reactiva, las reservas del usuario indicado ya hidratadas
 * con los datos de su habitación y usuario.
 *
 * Alimenta la pantalla "Mis Reservas" del cliente.
 */
class GetUserBookingsUseCase(private val bookingRepository: BookingRepository) {
    operator fun invoke(userId: Long): Flow<List<BookingWithDetails>> {
        return bookingRepository.getBookingsByUserId(userId)
    }
}