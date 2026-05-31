package com.example.jetpackstayrooms.domain

/**
 * Usuario del sistema, ya sea cliente o propietario del hostal.
 *
 * Modelo de dominio puro: no contiene anotaciones de persistencia ni datos
 * sensibles. El hash y el salt de la contraseña viven exclusivamente en
 * [com.example.jetpackstayrooms.data.local.entity.UserEntity] y nunca cruzan
 * la frontera de la capa de datos, por lo que cualquier `User` que circule
 * por dominio o UI es seguro de loggear, serializar o exponer en estado.
 *
 * @property id Identificador asignado por la base de datos. `0` indica que el
 *  usuario aún no ha sido persistido.
 * @property username Nombre de inicio de sesión. Debe ser único en todo el sistema.
 * @property isOwner `true` si el usuario es propietario del hostal y por tanto
 *  tiene acceso al panel de gestión; `false` para clientes.
 */
data class User(
    val id: Long = 0,
    val username: String,
    val name: String,
    val isOwner: Boolean = false
)