package com.aojeda.jetpackstayrooms.ui.screen

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
import com.aojeda.jetpackstayrooms.domain.BookingStatus
import com.aojeda.jetpackstayrooms.domain.BookingWithDetails
import com.aojeda.jetpackstayrooms.ui.navigation.Screen
import com.aojeda.jetpackstayrooms.ui.theme.*
import com.aojeda.jetpackstayrooms.ui.viewmodel.AuthViewModel
import com.aojeda.jetpackstayrooms.ui.viewmodel.BookingViewModel
import java.time.format.DateTimeFormatter

/**
 * Pantalla "Mis Reservas" del cliente.
 *
 * Carga las reservas del usuario en cuanto se conoce su identidad (mediante un
 * `LaunchedEffect(authState.currentUser)`) y permite cancelarlas si están en
 * estado [com.aojeda.jetpackstayrooms.domain.BookingStatus.ACTIVE]. Los
 * mensajes de éxito y error se muestran en snackbars que se auto-descartan a
 * los 2 segundos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    navController: NavController,
    bookingViewModel: BookingViewModel,
    authViewModel: AuthViewModel
) {
    val bookingState by bookingViewModel.bookingState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.currentUser) {
        authState.currentUser?.let {
            bookingViewModel.loadUserBookings(it.id)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mis Reservas",
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
                    TextButton(onClick = { navController.navigateUp() }) {
                        Text("← Volver", color = RustOrange)
                    }
                }
            )
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
            when {
                bookingState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = RustOrange
                    )
                }
                bookingState.bookings.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Sin reservas",
                            fontFamily = PlayfairDisplay,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate(Screen.RoomList.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = RustOrange)
                        ) {
                            Text("Ver habitaciones disponibles")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bookingState.bookings) { bookingWithDetails ->
                            BookingCard(
                                bookingWithDetails = bookingWithDetails,
                                onCancel = {
                                    bookingViewModel.cancelBooking(bookingWithDetails.booking.id)
                                }
                            )
                        }
                    }
                }
            }

            if (bookingState.successMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = SuccessGreen
                ) {
                    Text(bookingState.successMessage!!)
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    bookingViewModel.clearMessages()
                }
            }

            if (bookingState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = ErrorRed
                ) {
                    Text(bookingState.error!!)
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    bookingViewModel.clearMessages()
                }
            }
        }
    }
}

/**
 * Tarjeta con los datos de una reserva del cliente.
 *
 * @param onCancel Acción que dispara la cancelación; solo se ofrece visualmente
 *  cuando la reserva está en estado
 *  [com.aojeda.jetpackstayrooms.domain.BookingStatus.ACTIVE].
 */
@Composable
fun BookingCard(
    bookingWithDetails: BookingWithDetails,
    onCancel: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val booking = bookingWithDetails.booking
    val room = bookingWithDetails.room

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Habitación ${room.roomNumber}",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DeepNavy
                )

                StatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Check-in",
                        fontSize = 12.sp,
                        color = DeepNavy.copy(alpha = 0.6f)
                    )
                    Text(
                        booking.checkInDate.format(formatter),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DeepNavy
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Check-out",
                        fontSize = 12.sp,
                        color = DeepNavy.copy(alpha = 0.6f)
                    )
                    Text(
                        booking.checkOutDate.format(formatter),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DeepNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = DeepNavy.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total: ${booking.totalPrice.toInt()}€",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RustOrange
                )

                if (booking.status == BookingStatus.ACTIVE) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Etiqueta visual que mapea cada [BookingStatus] a un texto y un color.
 *
 * Se reutiliza tanto en la lista del cliente como en el panel del propietario.
 */
@Composable
fun StatusChip(status: BookingStatus) {
    val (text, color) = when (status) {
        BookingStatus.ACTIVE -> "Activa" to SuccessGreen
        BookingStatus.COMPLETED -> "Completada" to LightSage
        BookingStatus.CANCELLED -> "Cancelada" to ErrorRed
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}