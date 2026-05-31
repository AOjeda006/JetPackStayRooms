package com.example.jetpackstayrooms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jetpackstayrooms.domain.BookingStatus
import com.example.jetpackstayrooms.domain.BookingWithDetails
import com.example.jetpackstayrooms.ui.navigation.Screen
import com.example.jetpackstayrooms.ui.theme.*
import com.example.jetpackstayrooms.ui.viewmodel.AuthViewModel
import com.example.jetpackstayrooms.ui.viewmodel.OwnerViewModel
import java.time.format.DateTimeFormatter

/**
 * Panel del propietario.
 *
 * Hace de muro de seguridad básico: si el usuario actual no es propietario,
 * redirige al listado principal y aborta la composición antes de pintar nada.
 * No sustituye a una autorización real, pero evita que la pantalla se renderice
 * para usuarios sin permiso.
 *
 * Muestra una fila de estadísticas (total / disponibles / ocupadas) calculadas
 * en cliente a partir de [OwnerState.allRooms] y la lista de todas las reservas
 * del sistema con la acción "Finalizar" en las que aún estén activas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    navController: NavController,
    ownerViewModel: OwnerViewModel,
    authViewModel: AuthViewModel
) {
    val ownerState by ownerViewModel.ownerState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    if (authState.currentUser?.isOwner != true) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.RoomList.route) {
                popUpTo(Screen.RoomList.route) { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Panel del Propietario",
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = DeepNavy
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SoftCream
                ),
                navigationIcon = {
                    TextButton(onClick = { navController.navigate(Screen.RoomList.route) }) {
                        Text("← Inicio", color = RustOrange)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddRoom.route) },
                containerColor = RustOrange,
                contentColor = Color.White
            ) {
                Text("+ Agregar Habitación", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SoftCream, Color.White, LightSage.copy(alpha = 0.1f))
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatCard(
                            title = "Total Habitaciones",
                            value = ownerState.allRooms.size.toString(),
                            color = RustOrange
                        )
                        StatCard(
                            title = "Disponibles",
                            value = ownerState.allRooms.count { it.isAvailable }.toString(),
                            color = SuccessGreen
                        )
                        StatCard(
                            title = "Ocupadas",
                            value = ownerState.allRooms.count { !it.isAvailable }.toString(),
                            color = ErrorRed
                        )
                    }
                }

                Text(
                    "Todas las Reservas",
                    fontFamily = PlayfairDisplay,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ownerState.allBookings) { bookingWithDetails ->
                        OwnerBookingCard(
                            bookingWithDetails = bookingWithDetails,
                            onComplete = {
                                ownerViewModel.completeBooking(bookingWithDetails.booking.id)
                            }
                        )
                    }
                }
            }

            if (ownerState.successMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = SuccessGreen
                ) {
                    Text(ownerState.successMessage!!)
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    ownerViewModel.clearMessages()
                }
            }
        }
    }
}

/** Indicador numérico simple usado en la fila de estadísticas del dashboard. */
@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            title,
            fontSize = 12.sp,
            color = DeepNavy.copy(alpha = 0.6f)
        )
    }
}

/**
 * Tarjeta compacta de una reserva en el panel del propietario.
 *
 * @param onComplete Acción que dispara la finalización; solo se ofrece
 *  visualmente para reservas en estado
 *  [com.example.jetpackstayrooms.domain.BookingStatus.ACTIVE].
 */
@Composable
fun OwnerBookingCard(
    bookingWithDetails: BookingWithDetails,
    onComplete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM")
    val booking = bookingWithDetails.booking
    val room = bookingWithDetails.room
    val user = bookingWithDetails.user

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Hab. ${room.roomNumber} - ${user.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepNavy
                )
                Text(
                    "${booking.checkInDate.format(formatter)} - ${booking.checkOutDate.format(formatter)}",
                    fontSize = 13.sp,
                    color = DeepNavy.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(status = booking.status)
            }

            if (booking.status == BookingStatus.ACTIVE) {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Finalizar", fontSize = 12.sp)
                }
            }
        }
    }
}