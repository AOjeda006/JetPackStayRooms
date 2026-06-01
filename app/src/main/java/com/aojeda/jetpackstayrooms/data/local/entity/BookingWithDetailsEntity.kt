package com.aojeda.jetpackstayrooms.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.aojeda.jetpackstayrooms.domain.BookingWithDetails

/**
 * Resultado tabular de Room que combina una reserva con su habitación y usuario.
 *
 * Room rellena automáticamente:
 * - [booking] con las columnas de la tabla `bookings` (`@Embedded`).
 * - [room] resolviendo la columna `roomId` de la reserva contra la PK de `rooms`.
 * - [user] resolviendo `userId` contra la PK de `users`.
 *
 * Las consultas DAO que lo devuelven deben anotarse con `@Transaction` para que
 * la lectura del padre y la de los hijos ocurran en una sola transacción.
 */
data class BookingWithDetailsEntity(
    @Embedded val booking: BookingEntity,
    @Relation(
        parentColumn = "roomId",
        entityColumn = "id"
    )
    val room: RoomEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity
)

/** Convierte el agregado persistido en el modelo de dominio equivalente. */
fun BookingWithDetailsEntity.toDomain() = BookingWithDetails(
    booking = booking.toDomain(),
    room = room.toDomain(),
    user = user.toDomain()
)