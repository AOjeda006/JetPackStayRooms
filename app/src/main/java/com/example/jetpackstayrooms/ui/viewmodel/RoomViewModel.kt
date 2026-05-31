package com.example.jetpackstayrooms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackstayrooms.domain.Room
import com.example.jetpackstayrooms.domain.usecase.GetAvailableRoomsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado observable del listado de habitaciones disponibles.
 *
 * @property rooms Habitaciones disponibles tal y como las emite el caso de uso.
 */
data class RoomState(
    val rooms: List<Room> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Expone el listado de habitaciones disponibles a la pantalla principal.
 *
 * Suscribe el flujo de [GetAvailableRoomsUseCase] al construirse y vuelca cada
 * emisión en [roomState], por lo que la UI se actualiza automáticamente cuando
 * la disponibilidad cambia (p. ej. tras crear o cancelar una reserva).
 */
class RoomViewModel(
    private val getAvailableRoomsUseCase: GetAvailableRoomsUseCase
) : ViewModel() {

    private val _roomState = MutableStateFlow(RoomState())
    val roomState: StateFlow<RoomState> = _roomState.asStateFlow()

    private var roomsJob: Job? = null

    init {
        loadAvailableRooms()
    }

    /**
     * Vuelve a colectar el flujo de habitaciones disponibles. La UI la llama
     * tras un `logout` para asegurarse de que se muestra el estado más reciente.
     *
     * Cancela cualquier colección anterior antes de relanzarse para evitar
     * jobs zombi escribiendo en paralelo sobre el mismo [_roomState].
     */
    fun loadAvailableRooms() {
        roomsJob?.cancel()
        roomsJob = viewModelScope.launch {
            _roomState.value = _roomState.value.copy(isLoading = true)

            getAvailableRoomsUseCase().collect { rooms ->
                _roomState.value = _roomState.value.copy(
                    rooms = rooms,
                    isLoading = false,
                    error = null
                )
            }
        }
    }
}