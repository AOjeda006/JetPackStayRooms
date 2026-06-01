package com.aojeda.jetpackstayrooms.domain

/**
 * Reserva hidratada con los datos completos de su habitación y usuario asociados.
 *
 * Se usa en las listas donde la UI necesita mostrar más información de la que cabe
 * en [Booking] (p. ej. nombre del cliente, número de habitación) sin tener que
 * resolver las claves foráneas por separado.
 *
 * En la capa de datos se construye a partir de las relaciones `@Relation` de
 * [com.aojeda.jetpackstayrooms.data.local.entity.BookingWithDetailsEntity].
 */
data class BookingWithDetails(
    val booking: Booking,
    val room: Room,
    val user: User
)