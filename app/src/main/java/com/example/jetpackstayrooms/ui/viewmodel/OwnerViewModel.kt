package com.example.jetpackstayrooms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackstayrooms.domain.BookingWithDetails
import com.example.jetpackstayrooms.domain.Room
import com.example.jetpackstayrooms.domain.RoomType
import com.example.jetpackstayrooms.domain.repository.RoomRepository
import com.example.jetpackstayrooms.domain.usecase.AddRoomUseCase
import com.example.jetpackstayrooms.domain.usecase.CompleteBookingUseCase
import com.example.jetpackstayrooms.domain.usecase.GetAllBookingsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado observable del panel del propietario.
 *
 * @property allBookings Todas las reservas del sistema, ya hidratadas.
 * @property allRooms Todas las habitaciones del hostal (disponibles y no
 *  disponibles); alimenta las estadísticas del dashboard.
 */
data class OwnerState(
    val allBookings: List<BookingWithDetails> = emptyList(),
    val allRooms: List<Room> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * Acciones reservadas al propietario: dar de alta habitaciones, finalizar
 * reservas y ver agregados de todo el inventario.
 *
 * Suscribe en `init` los flujos de reservas y habitaciones, por lo que las
 * estadísticas y listados se mantienen al día sin intervención de la UI. La
 * dependencia directa de [RoomRepository] se usa para obtener `getAllRooms()`,
 * que el dominio expone fuera de cualquier caso de uso (no es una acción de
 * negocio, sino una consulta de soporte para el panel).
 */
class OwnerViewModel(
    private val addRoomUseCase: AddRoomUseCase,
    private val completeBookingUseCase: CompleteBookingUseCase,
    private val getAllBookingsUseCase: GetAllBookingsUseCase,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _ownerState = MutableStateFlow(OwnerState())
    val ownerState: StateFlow<OwnerState> = _ownerState.asStateFlow()

    private var bookingsJob: Job? = null
    private var roomsJob: Job? = null

    init {
        loadAllBookings()
        loadAllRooms()
    }

    /**
     * Cancela la suscripción anterior antes de relanzarla para que llamadas
     * repetidas no acumulen colectores escribiendo en el mismo [_ownerState].
     */
    fun loadAllBookings() {
        bookingsJob?.cancel()
        bookingsJob = viewModelScope.launch {
            _ownerState.value = _ownerState.value.copy(isLoading = true)

            getAllBookingsUseCase().collect { bookings ->
                _ownerState.value = _ownerState.value.copy(
                    allBookings = bookings,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    /** Equivalente a [loadAllBookings] para el listado completo de habitaciones. */
    fun loadAllRooms() {
        roomsJob?.cancel()
        roomsJob = viewModelScope.launch {
            roomRepository.getAllRooms().collect { rooms ->
                _ownerState.value = _ownerState.value.copy(
                    allRooms = rooms
                )
            }
        }
    }

    /**
     * Da de alta una habitación delegando validaciones en [AddRoomUseCase].
     * En éxito deja un mensaje en [OwnerState.successMessage] que la pantalla
     * usa para mostrar feedback y cerrarse automáticamente.
     */
    fun addRoom(
        roomNumber: String,
        type: RoomType,
        pricePerNight: Double,
        maxOccupancy: Int,
        description: String
    ) {
        viewModelScope.launch {
            _ownerState.value = _ownerState.value.copy(isLoading = true, error = null)

            val result = addRoomUseCase(roomNumber, type, pricePerNight, maxOccupancy, description)

            result.fold(
                onSuccess = {
                    _ownerState.value = _ownerState.value.copy(
                        isLoading = false,
                        successMessage = "Habitación agregada con éxito",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _ownerState.value = _ownerState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * Cierra una reserva activa (equivalente al check-out) y libera la habitación.
     */
    fun completeBooking(bookingId: Long) {
        viewModelScope.launch {
            _ownerState.value = _ownerState.value.copy(isLoading = true, error = null)

            val result = completeBookingUseCase(bookingId)

            result.fold(
                onSuccess = {
                    _ownerState.value = _ownerState.value.copy(
                        isLoading = false,
                        successMessage = "Reserva completada y habitación liberada",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _ownerState.value = _ownerState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /** Borra los mensajes transitorios (error y éxito) tras haberlos consumido la UI. */
    fun clearMessages() {
        _ownerState.value = _ownerState.value.copy(
            error = null,
            successMessage = null
        )
    }
}