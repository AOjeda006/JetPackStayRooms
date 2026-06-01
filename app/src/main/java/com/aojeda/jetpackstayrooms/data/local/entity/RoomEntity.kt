package com.aojeda.jetpackstayrooms.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aojeda.jetpackstayrooms.domain.Room
import com.aojeda.jetpackstayrooms.domain.RoomType

/**
 * Fila persistible de la tabla `rooms`, contrapartida en la capa de datos de [Room].
 *
 * @property type Se almacena como `String` (nombre del enum [RoomType]) en lugar
 *  de usar un `TypeConverter`. La conversión a/desde [RoomType] se hace en los
 *  mappers [toDomain] y [toEntity].
 */
@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomNumber: String,
    val type: String,
    val pricePerNight: Double,
    val maxOccupancy: Int,
    val description: String,
    val isAvailable: Boolean = true
)

/**
 * Convierte la fila persistida en el modelo de dominio.
 *
 * @throws IllegalArgumentException si `type` contiene un valor que no se
 *  corresponde con ninguna entrada de [RoomType].
 */
fun RoomEntity.toDomain() = Room(
    id = id,
    roomNumber = roomNumber,
    type = RoomType.valueOf(type),
    pricePerNight = pricePerNight,
    maxOccupancy = maxOccupancy,
    description = description,
    isAvailable = isAvailable
)

/** Convierte el modelo de dominio en la fila persistible (serializa [RoomType] por nombre). */
fun Room.toEntity() = RoomEntity(
    id = id,
    roomNumber = roomNumber,
    type = type.name,
    pricePerNight = pricePerNight,
    maxOccupancy = maxOccupancy,
    description = description,
    isAvailable = isAvailable
)