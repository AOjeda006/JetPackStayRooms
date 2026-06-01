package com.aojeda.jetpackstayrooms.domain.usecase

import com.aojeda.jetpackstayrooms.domain.BookingStatus
import com.aojeda.jetpackstayrooms.domain.repository.BookingRepository
import com.aojeda.jetpackstayrooms.domain.repository.RoomRepository

/**
 * Cierra una reserva activa marcándola como
 * [com.aojeda.jetpackstayrooms.domain.BookingStatus.COMPLETED] y libera la
 * habitación **si y solo si no quedan otras reservas activas sobre ella**.
 *
 * Es la acción equivalente al check-out: la dispara el propietario desde su
 * panel. Una reserva cancelada o ya finalizada no puede completarse de
 * nuevo (el caso de uso es idempotente: la segunda llamada falla en lugar de
 * volver a tocar la base de datos).
 *
 * Al igual que [CancelBookingUseCase], la disponibilidad se deriva del estado
 * real de las reservas vía
 * [com.aojeda.jetpackstayrooms.domain.repository.BookingRepository.countActiveBookingsByRoomId].
 */
class CompleteBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository
) {
    /**
     * @return [Result.success] si la transición se aplicó correctamente;
     *  [Result.failure] si la reserva no existe, si ya estaba cancelada o si
     *  falla la persistencia.
     */
    suspend operator fun invoke(bookingId: Long): Result<Unit> {
        return try {
            val booking = bookingRepository.getBookingById(bookingId)
                ?: return Result.failure(Exception("Reserva no encontrada"))

            if (booking.status == BookingStatus.CANCELLED) {
                return Result.failure(Exception("No se puede completar una reserva cancelada"))
            }

            if (booking.status == BookingStatus.COMPLETED) {
                return Result.failure(Exception("La reserva ya está finalizada"))
            }

            // Actualizar estado a completado
            bookingRepository.updateBooking(booking.copy(status = BookingStatus.COMPLETED))

            // Liberar la habitación solo si no quedan otras reservas activas
            if (bookingRepository.countActiveBookingsByRoomId(booking.roomId) == 0) {
                roomRepository.updateRoomAvailability(booking.roomId, true)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}