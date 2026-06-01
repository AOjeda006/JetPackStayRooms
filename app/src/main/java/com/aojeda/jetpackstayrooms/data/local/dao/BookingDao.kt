package com.aojeda.jetpackstayrooms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aojeda.jetpackstayrooms.data.local.entity.BookingEntity
import com.aojeda.jetpackstayrooms.data.local.entity.BookingWithDetailsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a la tabla `bookings`.
 *
 * Las consultas que devuelven [BookingWithDetailsEntity] usan `@Transaction`
 * porque Room debe leer la reserva y sus relaciones (`room`, `user`) en una sola
 * transacción para garantizar que el agregado sea consistente.
 */
@Dao
interface BookingDao {

    /**
     * @return Identificador autogenerado (rowId) de la reserva insertada.
     * @throws android.database.sqlite.SQLiteConstraintException si `userId` o
     *  `roomId` no apuntan a una fila existente (violación de la FK).
     */
    @Insert
    suspend fun insert(booking: BookingEntity): Long

    /**
     * Intenta ocupar la habitación de forma **atómica** mediante un
     * compare-and-set: actualiza `isAvailable = 0` solo si actualmente vale
     * `1`, y devuelve el número de filas afectadas (1 si se reservó, 0 si la
     * habitación ya estaba ocupada o no existía).
     *
     * Esta operación es la pieza que cierra la *race condition* en
     * [com.aojeda.jetpackstayrooms.domain.usecase.CreateBookingUseCase]:
     * usándola dentro de [insertBookingAtomically] dos clientes que pulsen
     * "Reservar" simultáneamente sobre la misma habitación se serializan en
     * el motor SQLite y solo uno obtiene la fila.
     */
    @Query("UPDATE rooms SET isAvailable = 0 WHERE id = :roomId AND isAvailable = 1")
    suspend fun tryOccupyRoom(roomId: Long): Int

    /**
     * Inserta una reserva ocupando atómicamente su habitación en la misma
     * transacción.
     *
     * @return rowId de la reserva insertada, o `-1` si la habitación ya no
     *  estaba disponible (en cuyo caso no se inserta nada y la transacción
     *  no muta el estado de la base de datos).
     */
    @Transaction
    suspend fun insertBookingAtomically(booking: BookingEntity): Long {
        val occupied = tryOccupyRoom(booking.roomId)
        if (occupied == 0) return -1L
        return insert(booking)
    }

    @Query("SELECT * FROM bookings WHERE id = :bookingId LIMIT 1")
    suspend fun getBookingById(bookingId: Long): BookingEntity?

    /** Reservas del usuario, hidratadas, más recientes primero. */
    @Transaction
    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY checkInDate DESC")
    fun getBookingsByUserId(userId: Long): Flow<List<BookingWithDetailsEntity>>

    /** Todas las reservas del sistema, hidratadas, más recientes primero. */
    @Transaction
    @Query("SELECT * FROM bookings ORDER BY checkInDate DESC")
    fun getAllBookingsWithDetails(): Flow<List<BookingWithDetailsEntity>>

    @Update
    suspend fun update(booking: BookingEntity)

    /**
     * Marca la reserva como `CANCELLED` sin reescribir el resto de columnas.
     *
     * Solo modifica la reserva: liberar la habitación es responsabilidad del
     * caso de uso que invoca esta operación.
     */
    @Query("UPDATE bookings SET status = 'CANCELLED' WHERE id = :bookingId")
    suspend fun cancelBooking(bookingId: Long)

    @Query("SELECT * FROM bookings WHERE roomId = :roomId AND status = 'ACTIVE'")
    fun getActiveBookingsByRoomId(roomId: Long): Flow<List<BookingEntity>>

    /**
     * Cuenta cuántas reservas en estado `ACTIVE` apuntan a la habitación
     * indicada. Lo usan los flujos de cancelación y finalización para
     * decidir si deben liberar la habitación o no.
     */
    @Query("SELECT COUNT(*) FROM bookings WHERE roomId = :roomId AND status = 'ACTIVE'")
    suspend fun countActiveBookingsByRoomId(roomId: Long): Int
}