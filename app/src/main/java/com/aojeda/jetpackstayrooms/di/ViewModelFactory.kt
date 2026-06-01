package com.aojeda.jetpackstayrooms.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aojeda.jetpackstayrooms.data.local.AppDatabase
import com.aojeda.jetpackstayrooms.data.repository.BookingRepositoryImpl
import com.aojeda.jetpackstayrooms.data.repository.RoomRepositoryImpl
import com.aojeda.jetpackstayrooms.data.repository.UserRepositoryImpl
import com.aojeda.jetpackstayrooms.domain.repository.BookingRepository
import com.aojeda.jetpackstayrooms.domain.repository.RoomRepository
import com.aojeda.jetpackstayrooms.domain.repository.UserRepository
import com.aojeda.jetpackstayrooms.domain.usecase.*
import com.aojeda.jetpackstayrooms.ui.viewmodel.AuthViewModel
import com.aojeda.jetpackstayrooms.ui.viewmodel.BookingViewModel
import com.aojeda.jetpackstayrooms.ui.viewmodel.OwnerViewModel
import com.aojeda.jetpackstayrooms.ui.viewmodel.RoomViewModel

/**
 * Composition root manual de la aplicación.
 *
 * Cablea las dependencias de las tres capas (base de datos → repositorios →
 * casos de uso → ViewModels) sin usar un framework de inyección de
 * dependencias. Cada componente se crea con `by lazy` para que su construcción
 * se aplace hasta el primer acceso, manteniendo cada uno como instancia única
 * dentro del factory.
 *
 * Se entrega a `AppNavigation`, que la usa para resolver los ViewModels que
 * cada pantalla consume mediante el helper `viewModel(factory = ...)`.
 *
 * @property context Se conserva solo para obtener la base de datos
 *  ([AppDatabase.getDatabase] internamente extrae el `applicationContext`).
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val database by lazy { AppDatabase.getDatabase(context) }

    // Repositories
    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userDao())
    }

    private val roomRepository: RoomRepository by lazy {
        RoomRepositoryImpl(database.roomDao())
    }

    private val bookingRepository: BookingRepository by lazy {
        BookingRepositoryImpl(database.bookingDao())
    }

    // Use Cases
    private val registerUserUseCase by lazy { RegisterUserUseCase(userRepository) }
    private val loginUserUseCase by lazy { LoginUserUseCase(userRepository) }
    private val getAvailableRoomsUseCase by lazy { GetAvailableRoomsUseCase(roomRepository) }
    private val createBookingUseCase by lazy { CreateBookingUseCase(bookingRepository, roomRepository) }
    private val cancelBookingUseCase by lazy { CancelBookingUseCase(bookingRepository, roomRepository) }
    private val getUserBookingsUseCase by lazy { GetUserBookingsUseCase(bookingRepository) }
    private val addRoomUseCase by lazy { AddRoomUseCase(roomRepository) }
    private val completeBookingUseCase by lazy { CompleteBookingUseCase(bookingRepository, roomRepository) }
    private val getAllBookingsUseCase by lazy { GetAllBookingsUseCase(bookingRepository) }

    /**
     * Instancia el ViewModel solicitado con sus dependencias ya cableadas.
     *
     * @throws IllegalArgumentException si [modelClass] no es uno de los
     *  ViewModels conocidos por este factory.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(
                    registerUserUseCase = registerUserUseCase,
                    loginUserUseCase = loginUserUseCase
                ) as T
            }
            modelClass.isAssignableFrom(RoomViewModel::class.java) -> {
                RoomViewModel(
                    getAvailableRoomsUseCase = getAvailableRoomsUseCase
                ) as T
            }
            modelClass.isAssignableFrom(BookingViewModel::class.java) -> {
                BookingViewModel(
                    createBookingUseCase = createBookingUseCase,
                    cancelBookingUseCase = cancelBookingUseCase,
                    getUserBookingsUseCase = getUserBookingsUseCase
                ) as T
            }
            modelClass.isAssignableFrom(OwnerViewModel::class.java) -> {
                OwnerViewModel(
                    addRoomUseCase = addRoomUseCase,
                    completeBookingUseCase = completeBookingUseCase,
                    getAllBookingsUseCase = getAllBookingsUseCase,
                    roomRepository = roomRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}