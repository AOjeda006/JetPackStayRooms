package com.example.jetpackstayrooms.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.jetpackstayrooms.domain.Booking
import com.example.jetpackstayrooms.domain.BookingStatus
import java.time.LocalDate

/**
 * Fila persistible de la tabla `bookings`, contrapartida en la capa de datos de [Booking].
 *
 * Define dos claves foráneas (`userId`, `roomId`) con borrado en cascada: al
 * eliminar un usuario o una habitación, todas sus reservas desaparecen junto
 * con ellos. Los índices secundarios sobre esas mismas columnas evitan los
 * escaneos de tabla completos que Room exige por defecto cuando hay FKs.
 *
 * Las fechas se almacenan como `String` en formato ISO-8601 (`YYYY-MM-DD`) para
 * evitar tener que registrar un `TypeConverter` para [java.time.LocalDate]; los
 * mappers se encargan del parseo.
 *
 * @property status Nombre del enum [BookingStatus] persistido como `String`,
 *  con el mismo criterio que `checkInDate`/`checkOutDate`.
 */
@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["roomId"])
    ]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val roomId: Long,
    val checkInDate: String,
    val checkOutDate: String,
    val status: String,
    val totalPrice: Double
)

/**
 * Convierte la fila persistida en el modelo de dominio.
 *
 * @throws java.time.format.DateTimeParseException si las cadenas de fecha no
 *  están en formato ISO-8601 válido.
 * @throws IllegalArgumentException si `status` no se corresponde con ninguna
 *  entrada de [BookingStatus].
 */
fun BookingEntity.toDomain() = Booking(
    id = id,
    userId = userId,
    roomId = roomId,
    checkInDate = LocalDate.parse(checkInDate),
    checkOutDate = LocalDate.parse(checkOutDate),
    status = BookingStatus.valueOf(status),
    totalPrice = totalPrice
)

/**
 * Convierte el modelo de dominio en la fila persistible, serializando fechas a
 * ISO-8601 y [BookingStatus] por su `name`.
 */
fun Booking.toEntity() = BookingEntity(
    id = id,
    userId = userId,
    roomId = roomId,
    checkInDate = checkInDate.toString(),
    checkOutDate = checkOutDate.toString(),
    status = status.name,
    totalPrice = totalPrice
)