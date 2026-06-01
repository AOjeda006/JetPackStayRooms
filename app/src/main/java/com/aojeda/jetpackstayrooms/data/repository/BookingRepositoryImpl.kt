package com.aojeda.jetpackstayrooms.data.repository

import com.aojeda.jetpackstayrooms.data.local.dao.BookingDao
import com.aojeda.jetpackstayrooms.data.local.entity.toDomain
import com.aojeda.jetpackstayrooms.data.local.entity.toEntity
import com.aojeda.jetpackstayrooms.domain.Booking
import com.aojeda.jetpackstayrooms.domain.BookingWithDetails
import com.aojeda.jetpackstayrooms.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación de [BookingRepository] respaldada por Room.
 *
 * Las funciones que devuelven [BookingWithDetails] explotan las relaciones
 * `@Relation` definidas en
 * [com.aojeda.jetpackstayrooms.data.local.entity.BookingWithDetailsEntity] para
 * obtener en una sola consulta la reserva junto con su habitación y usuario.
 */
class BookingRepositoryImpl(private val bookingDao: BookingDao) : BookingRepository {
    override suspend fun insertBooking(booking: Booking): Long {
        return bookingDao.insert(booking.toEntity())
    }

    override suspend fun insertBookingAtomically(booking: Booking): Long {
        return bookingDao.insertBookingAtomically(booking.toEntity())
    }

    override suspend fun getBookingById(bookingId: Long): Booking? {
        return bookingDao.getBookingById(bookingId)?.toDomain()
    }

    override fun getBookingsByUserId(userId: Long): Flow<List<BookingWithDetails>> {
        return bookingDao.getBookingsByUserId(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllBookingsWithDetails(): Flow<List<BookingWithDetails>> {
        return bookingDao.getAllBookingsWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun updateBooking(booking: Booking) {
        bookingDao.update(booking.toEntity())
    }

    override suspend fun cancelBooking(bookingId: Long) {
        bookingDao.cancelBooking(bookingId)
    }

    override fun getActiveBookingsByRoomId(roomId: Long): Flow<List<Booking>> {
        return bookingDao.getActiveBookingsByRoomId(roomId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun countActiveBookingsByRoomId(roomId: Long): Int {
        return bookingDao.countActiveBookingsByRoomId(roomId)
    }
}