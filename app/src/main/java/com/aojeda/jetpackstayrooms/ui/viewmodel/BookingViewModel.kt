package com.aojeda.jetpackstayrooms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aojeda.jetpackstayrooms.domain.BookingWithDetails
import com.aojeda.jetpackstayrooms.domain.usecase.CancelBookingUseCase
import com.aojeda.jetpackstayrooms.domain.usecase.CreateBookingUseCase
import com.aojeda.jetpackstayrooms.domain.usecase.GetUserBookingsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Estado observable de la pantalla "Mis Reservas" del cliente.
 *
 * @property bookings Reservas del usuario actual, hidratadas con habitación y usuario.
 * @property isBookingSuccess Bandera de un solo uso que se pone a `true` al crear
 *  una reserva con éxito; la UI la consume para navegar a la lista de reservas
 *  y debe limpiarla con [BookingViewModel.clearMessages].
 * @property successMessage Mensaje breve de confirmación para mostrar en un
 *  snackbar. La UI lo limpia tras presentarlo.
 */
data class BookingState(
    val bookings: List<BookingWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isBookingSuccess: Boolean = false,
    val successMessage: String? = null
)

/**
 * Acciones de reserva accesibles para el cliente: crear, cancelar y listar las
 * propias.
 *
 * Se asume que el `userId` ya viene resuelto desde la sesión activa
 * ([com.aojeda.jetpackstayrooms.ui.viewmodel.AuthViewModel]); este ViewModel no
 * comprueba la autenticación.
 */
class BookingViewModel(
    private val createBookingUseCase: CreateBookingUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val getUserBookingsUseCase: GetUserBookingsUseCase
) : ViewModel() {

    private val _bookingState = MutableStateFlow(BookingState())
    val bookingState: StateFlow<BookingState> = _bookingState.asStateFlow()

    private var userBookingsJob: Job? = null

    /**
     * Suscribe en `viewModelScope` el flujo de reservas del usuario y vuelca
     * cada emisión en [bookingState]. La UI debe llamarla cuando se conozca
     * el [userId] (típicamente desde un `LaunchedEffect` que observa la sesión).
     *
     * Cancela la suscripción anterior antes de relanzarse, de modo que un
     * cambio de usuario o una re-entrada a la pantalla no acumule colectores.
     */
    fun loadUserBookings(userId: Long) {
        userBookingsJob?.cancel()
        userBookingsJob = viewModelScope.launch {
            _bookingState.value = _bookingState.value.copy(isLoading = true)

            getUserBookingsUseCase(userId).collect { bookings ->
                _bookingState.value = _bookingState.value.copy(
                    bookings = bookings,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    /**
     * Crea una reserva delegando todas las validaciones de negocio en
     * [CreateBookingUseCase]. En éxito activa [BookingState.isBookingSuccess] y
     * deja un mensaje en [BookingState.successMessage]; en fallo expone el
     * mensaje de error en [BookingState.error].
     */
    fun createBooking(
        userId: Long,
        roomId: Long,
        checkInDate: LocalDate,
        checkOutDate: LocalDate
    ) {
        viewModelScope.launch {
            _bookingState.value = _bookingState.value.copy(isLoading = true, error = null)

            val result = createBookingUseCase(userId, roomId, checkInDate, checkOutDate)

            result.fold(
                onSuccess = {
                    _bookingState.value = _bookingState.value.copy(
                        isLoading = false,
                        isBookingSuccess = true,
                        successMessage = "Reserva realizada con éxito",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _bookingState.value = _bookingState.value.copy(
                        isLoading = false,
                        error = exception.message,
                        isBookingSuccess = false
                    )
                }
            )
        }
    }

    /**
     * Cancela una reserva existente. Una vez completada, el flujo reactivo de
     * [loadUserBookings] emitirá el estado actualizado sin necesidad de
     * recargar manualmente.
     */
    fun cancelBooking(bookingId: Long) {
        viewModelScope.launch {
            _bookingState.value = _bookingState.value.copy(isLoading = true, error = null)

            val result = cancelBookingUseCase(bookingId)

            result.fold(
                onSuccess = {
                    _bookingState.value = _bookingState.value.copy(
                        isLoading = false,
                        successMessage = "Reserva cancelada con éxito",
                        error = null
                    )
                },
                onFailure = { exception ->
                    _bookingState.value = _bookingState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    /**
     * Borra mensajes transitorios (error, mensaje de éxito y bandera
     * `isBookingSuccess`) tras haberlos consumido la UI, para que no vuelvan a
     * disparar efectos.
     */
    fun clearMessages() {
        _bookingState.value = _bookingState.value.copy(
            error = null,
            successMessage = null,
            isBookingSuccess = false
        )
    }
}