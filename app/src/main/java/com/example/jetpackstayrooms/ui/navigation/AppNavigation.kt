package com.example.jetpackstayrooms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.jetpackstayrooms.di.ViewModelFactory
import com.example.jetpackstayrooms.ui.screen.*
import com.example.jetpackstayrooms.ui.viewmodel.AuthViewModel
import com.example.jetpackstayrooms.ui.viewmodel.BookingViewModel
import com.example.jetpackstayrooms.ui.viewmodel.OwnerViewModel
import com.example.jetpackstayrooms.ui.viewmodel.RoomViewModel

/**
 * Grafo de navegación raíz de la aplicación.
 *
 * Resuelve los cuatro ViewModels en el ámbito del `NavHost` y los pasa a las
 * pantallas que los necesitan. Compartirlos a este nivel hace que el estado
 * (sesión, listado de habitaciones, reservas) sobreviva al salto entre
 * pantallas sin tener que persistirlo manualmente.
 *
 * El destino inicial es [Screen.RoomList], al que se accede sin haber iniciado
 * sesión: la autenticación es opcional para navegar pero obligatoria para
 * reservar.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModelFactory: ViewModelFactory
) {
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val roomViewModel: RoomViewModel = viewModel(factory = viewModelFactory)
    val bookingViewModel: BookingViewModel = viewModel(factory = viewModelFactory)
    val ownerViewModel: OwnerViewModel = viewModel(factory = viewModelFactory)

    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.RoomList.route
    ) {
        composable(Screen.RoomList.route) {
            RoomListScreen(
                navController = navController,
                roomViewModel = roomViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.RoomDetail.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            RoomDetailScreen(
                roomId = roomId,
                navController = navController,
                roomViewModel = roomViewModel,
                bookingViewModel = bookingViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.BookingList.route) {
            BookingListScreen(
                navController = navController,
                bookingViewModel = bookingViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.OwnerDashboard.route) {
            OwnerDashboardScreen(
                navController = navController,
                ownerViewModel = ownerViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.AddRoom.route) {
            AddRoomScreen(
                navController = navController,
                ownerViewModel = ownerViewModel
            )
        }
    }
}