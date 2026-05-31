package com.example.jetpackstayrooms.domain.usecase

import com.example.jetpackstayrooms.domain.BookingWithDetails
import com.example.jetpackstayrooms.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Devuelve, de forma reactiva, todas las reservas del sistema hidratadas con
 * los datos de su habitación y usuario.
 *
 * Alimenta el panel del propietario; los clientes deben usar
 * [GetUserBookingsUseCase] para ver únicamente las suyas.
 */
class GetAllBookingsUseCase(private val bookingRepository: BookingRepository) {
    operator fun invoke(): Flow<List<BookingWithDetails>> {
        return bookingRepository.getAllBookingsWithDetails()
    }
}