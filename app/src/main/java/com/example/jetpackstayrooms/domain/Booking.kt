package com.example.jetpackstayrooms.domain

import java.time.LocalDate

/**
 * Reserva de una habitación realizada por un usuario para un rango de fechas.
 *
 * Las restricciones de negocio (`checkOutDate > checkInDate`, `checkInDate >= hoy`,
 * habitación disponible, etc.) se aplican en
 * [com.example.jetpackstayrooms.domain.usecase.CreateBookingUseCase], no aquí.
 *
 * @property id Identificador asignado por la base de datos. `0` indica reserva
 *  aún no persistida.
 * @property userId Clave foránea hacia [User.id]. La reserva se elimina en cascada
 *  si el usuario es borrado.
 * @property roomId Clave foránea hacia [Room.id]. La reserva se elimina en cascada
 *  si la habitación es borrada.
 * @property totalPrice Importe total calculado como `noches * pricePerNight` al
 *  crear la reserva; no se recalcula tras cambios de precio.
 */
data class Booking(
    val id: Long = 0,
    val userId: Long,
    val roomId: Long,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val status: BookingStatus,
    val totalPrice: Double
)

/**
 * Estado del ciclo de vida de una reserva.
 *
 * Transiciones válidas:
 * - [ACTIVE] -> [COMPLETED] (cierre por parte del propietario)
 * - [ACTIVE] -> [CANCELLED] (cancelación por parte del cliente o propietario)
 *
 * Una reserva en [COMPLETED] o [CANCELLED] no puede volver a [ACTIVE].
 */
enum class BookingStatus {
    /** Reserva vigente que ocupa la habitación. */
    ACTIVE,

    /** Estancia finalizada con normalidad; libera la habitación. */
    COMPLETED,

    /** Reserva anulada antes de su finalización; libera la habitación. */
    CANCELLED
}