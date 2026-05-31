package com.example.jetpackstayrooms.domain.usecase

import com.example.jetpackstayrooms.domain.Booking
import com.example.jetpackstayrooms.domain.BookingStatus
import com.example.jetpackstayrooms.domain.repository.BookingRepository
import com.example.jetpackstayrooms.domain.repository.RoomRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Crea una nueva reserva validando reglas de negocio y bloqueando la habitación.
 *
 * Coordina el repositorio de reserva (atómico) y el de habitación así:
 * 1. Valida fechas localmente.
 * 2. Recupera la habitación para calcular el precio total (`noches * pricePerNight`).
 * 3. Llama a [BookingRepository.insertBookingAtomically], que ejecuta en una
 *    única transacción SQLite el compare-and-set sobre `isAvailable` y el
 *    `INSERT` de la reserva. Si la habitación deja de estar disponible justo
 *    antes (otro cliente la reservó), el método devuelve `-1` y este caso de
 *    uso retorna un fallo sin que la base de datos cambie.
 *
 * Esta atomicidad es la que elimina la *race condition* clásica de "leer y
 * luego escribir": ya no existe ventana entre comprobar la disponibilidad e
 * insertar la reserva.
 */
class CreateBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository
) {
    /**
     * @param userId Identificador del cliente que realiza la reserva.
     * @param roomId Identificador de la habitación a reservar.
     * @param checkInDate Fecha de entrada; debe ser hoy o posterior.
     * @param checkOutDate Fecha de salida; debe ser estrictamente posterior a
     *  [checkInDate].
     * @return [Result.success] con la reserva persistida (incluye `id` y precio
     *  calculado); [Result.failure] si las fechas son inválidas, si la habitación
     *  no existe, si otro cliente se ha adelantado a reservarla o si ocurre un
     *  error de persistencia.
     */
    suspend operator fun invoke(
        userId: Long,
        roomId: Long,
        checkInDate: LocalDate,
        checkOutDate: LocalDate
    ): Result<Booking> {
        return try {
            // 1. Validaciones de fecha (locales)
            if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
                return Result.failure(Exception("La fecha de salida debe ser posterior a la fecha de entrada"))
            }

            if (checkInDate.isBefore(LocalDate.now())) {
                return Result.failure(Exception("La fecha de entrada no puede ser anterior a hoy"))
            }

            // 2. Recuperar habitación para calcular precio
            val room = roomRepository.getRoomById(roomId)
                ?: return Result.failure(Exception("Habitación no encontrada"))

            val nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate)
            val totalPrice = nights * room.pricePerNight

            val booking = Booking(
                userId = userId,
                roomId = roomId,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                status = BookingStatus.ACTIVE,
                totalPrice = totalPrice
            )

            // 3. Insertar + ocupar habitación en una sola transacción
            val bookingId = bookingRepository.insertBookingAtomically(booking)
            if (bookingId == -1L) {
                return Result.failure(Exception("La habitación no está disponible"))
            }

            Result.success(booking.copy(id = bookingId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}