package com.example.jetpackstayrooms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.jetpackstayrooms.ui.navigation.Screen
import com.example.jetpackstayrooms.ui.theme.*
import com.example.jetpackstayrooms.ui.viewmodel.AuthViewModel
import com.example.jetpackstayrooms.ui.viewmodel.BookingViewModel
import com.example.jetpackstayrooms.ui.viewmodel.RoomViewModel
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * Detalle de una habitación con el formulario de reserva embebido.
 *
 * Resuelve la habitación localmente sobre `roomState.rooms` en lugar de
 * consultar el repositorio, aprovechando el flujo que [RoomViewModel] ya tiene
 * cargado. Si la habitación no aparece en ese listado (p. ej. porque dejó de
 * estar disponible) muestra un mensaje de "Habitación no encontrada".
 *
 * El bloque de reserva solo se pinta para usuarios autenticados que no son
 * propietarios; al confirmar, navega a la lista de reservas y limpia el stack
 * hasta el listado principal.
 *
 * @param roomId Identificador resuelto desde la ruta de navegación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: Long,
    navController: NavController,
    roomViewModel: RoomViewModel,
    bookingViewModel: BookingViewModel,
    authViewModel: AuthViewModel
) {
    val roomState by roomViewModel.roomState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val bookingState by bookingViewModel.bookingState.collectAsState()

    val room = roomState.rooms.find { it.id == roomId }

    var checkInDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var checkOutDate by remember { mutableStateOf(LocalDate.now().plusDays(2)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectingCheckIn by remember { mutableStateOf(true) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(bookingState.isBookingSuccess) {
        if (bookingState.isBookingSuccess) {
            bookingViewModel.clearMessages()
            navController.navigate(Screen.BookingList.route) {
                popUpTo(Screen.RoomList.route)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Detalles de Habitación", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SoftCream),
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
                .background(Brush.verticalGradient(listOf(SoftCream, Color.White)))
                .padding(padding)
        ) {

            if (room != null) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text("Habitación ${room.roomNumber}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(room.description)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("${room.pricePerNight.toInt()}€ / noche", color = RustOrange, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (authState.currentUser != null && !authState.currentUser!!.isOwner) {

                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.padding(24.dp)) {

                                Text("Realizar Reserva", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.height(20.dp))

                                // ---------- CHECK IN ----------
                                Box(Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = checkInDate.format(formatter),
                                        onValueChange = {},
                                        label = { Text("Fecha de entrada") },
                                        readOnly = true,
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Box(
                                        Modifier
                                            .matchParentSize()
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                selectingCheckIn = true
                                                showDatePicker = true
                                            }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ---------- CHECK OUT ----------
                                Box(Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = checkOutDate.format(formatter),
                                        onValueChange = {},
                                        label = { Text("Fecha de salida") },
                                        readOnly = true,
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Box(
                                        Modifier
                                            .matchParentSize()
                                            .clickable(
                                                interactionSource = interactionSource,
                                                indication = null
                                            ) {
                                                selectingCheckIn = false
                                                showDatePicker = true
                                            }
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        bookingViewModel.createBooking(
                                            userId = authState.currentUser!!.id,
                                            roomId = room.id,
                                            checkInDate = checkInDate,
                                            checkOutDate = checkOutDate
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Confirmar Reserva")
                                }
                            }
                        }
                    }
                }

            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Habitación no encontrada")
                }
            }

            // Feedback de error de reserva (p. ej. fechas inválidas o habitación
            // ya ocupada por otro cliente): se muestra y se auto-descarta.
            if (bookingState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = ErrorRed
                ) {
                    Text(bookingState.error!!)
                }
                LaunchedEffect(bookingState.error) {
                    kotlinx.coroutines.delay(3000)
                    bookingViewModel.clearMessages()
                }
            }

            // ---------------- DATE PICKER ----------------
            if (showDatePicker) {

                // Cota mínima de selección: hoy para check-in,
                // día siguiente al check-in para check-out
                val minSelectableDate = if (selectingCheckIn) LocalDate.now()
                                        else checkInDate.plusDays(1)
                val minSelectableMillis = minSelectableDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val selectableDates = remember(minSelectableMillis) {
                    object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                            utcTimeMillis >= minSelectableMillis
                    }
                }

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = (if (selectingCheckIn) checkInDate else checkOutDate)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
                    selectableDates = selectableDates
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()

                                if (selectingCheckIn) {
                                    checkInDate = selectedDate
                                    if (!checkOutDate.isAfter(checkInDate)) {
                                        checkOutDate = checkInDate.plusDays(1)
                                    }
                                } else {
                                    if (selectedDate.isAfter(checkInDate)) {
                                        checkOutDate = selectedDate
                                    }
                                }
                            }
                            showDatePicker = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}
