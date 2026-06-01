package com.aojeda.jetpackstayrooms.domain.repository

import com.aojeda.jetpackstayrooms.domain.Booking
import com.aojeda.jetpackstayrooms.domain.BookingWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * Puerto de acceso a reservas definido por el dominio.
 *
 * Expone tanto la entidad plana [Booking] como su variante hidratada
 * [BookingWithDetails], que la UI usa para mostrar listados sin tener que
 * resolver claves foráneas manualmente.
 */
interface BookingRepository {

    /**
     * Persiste una nueva reserva.
     *
     * @return Identificador autogenerado de la reserva insertada.
     */
    suspend fun insertBooking(booking: Booking): Long

    /**
     * Inserta la reserva y marca su habitación como ocupada en la **misma
     * transacción de base de datos**, mediante un compare-and-set sobre
     * `isAvailable`.
     *
     * Es la operación que garantiza que dos reservas concurrentes sobre la
     * misma habitación nunca puedan persistirse a la vez: el motor SQLite
     * serializa la transacción y solo la primera obtiene la fila.
     *
     * @return Identificador autogenerado de la reserva, o `-1` si la
     *  habitación ya no estaba disponible cuando la transacción se ejecutó
     *  (en cuyo caso la base de datos queda exactamente como estaba).
     */
    suspend fun insertBookingAtomically(booking: Booking): Long

    /**
     * @return La reserva con el [bookingId] dado, o `null` si no existe.
     */
    suspend fun getBookingById(bookingId: Long): Booking?

    /**
     * Flujo reactivo con las reservas del usuario ordenadas por fecha de entrada
     * descendente y enriquecidas con datos de habitación y usuario.
     */
    fun getBookingsByUserId(userId: Long): Flow<List<BookingWithDetails>>

    /**
     * Flujo reactivo con todas las reservas del sistema (vista de propietario),
     * ordenadas por fecha de entrada descendente.
     */
    fun getAllBookingsWithDetails(): Flow<List<BookingWithDetails>>

    /**
     * Sobrescribe todos los campos de una reserva existente. Usado, entre otros,
     * para transicionar a `COMPLETED`.
     */
    suspend fun updateBooking(booking: Booking)

    /**
     * Marca la reserva como `CANCELLED` sin tocar el resto de campos.
     *
     * No libera la habitación por sí solo; esa decisión la toma
     * [com.aojeda.jetpackstayrooms.domain.usecase.CancelBookingUseCase].
     */
    suspend fun cancelBooking(bookingId: Long)

    /**
     * Flujo con las reservas en estado [com.aojeda.jetpackstayrooms.domain.BookingStatus.ACTIVE]
     * que ocupan la habitación indicada.
     */
    fun getActiveBookingsByRoomId(roomId: Long): Flow<List<Booking>>

    /**
     * Cuenta cuántas reservas en estado
     * [com.aojeda.jetpackstayrooms.domain.BookingStatus.ACTIVE] apuntan a la
     * habitación dada. Lo usan los casos de uso de cancelación y finalización
     * para decidir si la habitación debe liberarse (solo si el contador llega
     * a 0).
     */
    suspend fun countActiveBookingsByRoomId(roomId: Long): Int
}