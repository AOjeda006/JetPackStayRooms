package com.aojeda.jetpackstayrooms.domain.usecase

import com.aojeda.jetpackstayrooms.domain.BookingStatus
import com.aojeda.jetpackstayrooms.domain.repository.BookingRepository
import com.aojeda.jetpackstayrooms.domain.repository.RoomRepository

/**
 * Cancela una reserva activa y libera la habitación que ocupaba **si y solo
 * si no quedan otras reservas activas sobre ella**.
 *
 * La disponibilidad se deriva del estado real de las reservas, no se asume:
 * tras cancelar, se consulta cuántas reservas activas siguen apuntando a la
 * misma habitación; solo si esa cuenta llega a cero se restablece
 * `isAvailable = true`. Esto evita liberar prematuramente una habitación si
 * por cualquier motivo coexistiera más de una reserva activa sobre ella.
 *
 * Rechaza la operación si la reserva ya está
 * [com.aojeda.jetpackstayrooms.domain.BookingStatus.COMPLETED] o
 * [com.aojeda.jetpackstayrooms.domain.BookingStatus.CANCELLED]: el ciclo de
 * vida de una reserva es terminal en esos estados.
 */
class CancelBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository
) {
    /**
     * @return [Result.success] si la cancelación se aplicó correctamente;
     *  [Result.failure] si la reserva no existe, ya estaba finalizada o cancelada,
     *  o si falló la persistencia.
     */
    suspend operator fun invoke(bookingId: Long): Result<Unit> {
        return try {
            val booking = bookingRepository.getBookingById(bookingId)
                ?: return Result.failure(Exception("Reserva no encontrada"))

            if (booking.status == BookingStatus.COMPLETED) {
                return Result.failure(Exception("No se puede cancelar una reserva finalizada"))
            }

            if (booking.status == BookingStatus.CANCELLED) {
                return Result.failure(Exception("La reserva ya está cancelada"))
            }

            // Cancelar la reserva
            bookingRepository.cancelBooking(bookingId)

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