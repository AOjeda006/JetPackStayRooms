package com.aojeda.jetpackstayrooms.domain.usecase

import com.aojeda.jetpackstayrooms.domain.Room
import com.aojeda.jetpackstayrooms.domain.RoomType
import com.aojeda.jetpackstayrooms.domain.repository.RoomRepository

/**
 * Da de alta una nueva habitación en el inventario del hostal.
 *
 * Solo se usa desde el panel del propietario; aplica validaciones básicas sobre
 * los campos numéricos y deja la habitación marcada como disponible.
 */
class AddRoomUseCase(private val roomRepository: RoomRepository) {
    /**
     * @param pricePerNight Precio en euros por noche; debe ser estrictamente > 0.
     * @param maxOccupancy Capacidad máxima; debe ser estrictamente > 0.
     * @return [Result.success] con la habitación persistida; [Result.failure] si
     *  el número de habitación viene vacío, si el precio o la ocupación son
     *  inválidos, o si falla la persistencia.
     */
    suspend operator fun invoke(
        roomNumber: String,
        type: RoomType,
        pricePerNight: Double,
        maxOccupancy: Int,
        description: String
    ): Result<Room> {
        return try {
            if (roomNumber.isBlank()) {
                return Result.failure(Exception("El número de habitación es obligatorio"))
            }

            if (pricePerNight <= 0) {
                return Result.failure(Exception("El precio debe ser mayor a 0"))
            }

            if (maxOccupancy <= 0) {
                return Result.failure(Exception("La ocupación máxima debe ser mayor a 0"))
            }

            val room = Room(
                roomNumber = roomNumber,
                type = type,
                pricePerNight = pricePerNight,
                maxOccupancy = maxOccupancy,
                description = description,
                isAvailable = true
            )

            val roomId = roomRepository.insertRoom(room)
            Result.success(room.copy(id = roomId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}