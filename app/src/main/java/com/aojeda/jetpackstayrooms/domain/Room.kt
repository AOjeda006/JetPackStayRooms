package com.aojeda.jetpackstayrooms.domain

/**
 * Habitación reservable del hostal.
 *
 * @property id Identificador asignado por la base de datos. `0` indica que aún
 *  no ha sido persistida.
 * @property roomNumber Número visible al usuario (p. ej. "101", "202"). No es
 *  necesariamente numérico ni se valida como único.
 * @property pricePerNight Precio en euros por noche. Debe ser estrictamente positivo.
 * @property maxOccupancy Número máximo de huéspedes admitidos. Debe ser >= 1.
 * @property isAvailable `true` si está libre para nuevas reservas. Se marca
 *  `false` mientras exista una reserva activa que la ocupe.
 */
data class Room(
    val id: Long = 0,
    val roomNumber: String,
    val type: RoomType,
    val pricePerNight: Double,
    val maxOccupancy: Int,
    val description: String,
    val isAvailable: Boolean = true
)

/**
 * Categoría comercial de la habitación, ordenada de menor a mayor prestación.
 *
 * Se persiste por nombre (`name`) en la columna `type` de la tabla `rooms`.
 */
enum class RoomType {
    SINGLE,
    DOUBLE,
    SUITE,
    DELUXE
}