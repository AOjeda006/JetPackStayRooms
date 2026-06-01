package com.aojeda.jetpackstayrooms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aojeda.jetpackstayrooms.domain.RoomType
import com.aojeda.jetpackstayrooms.ui.theme.*
import com.aojeda.jetpackstayrooms.ui.viewmodel.OwnerViewModel

/**
 * Formulario de alta de una nueva habitación.
 *
 * Tras un alta exitosa se espera 2 segundos (para que el snackbar de
 * confirmación sea visible), se limpia el estado del ViewModel y se navega
 * atrás. Las validaciones de negocio (precio > 0, ocupación > 0, etc.) las
 * aplica [com.aojeda.jetpackstayrooms.domain.usecase.AddRoomUseCase]; esta
 * pantalla solo limita el teclado y convierte los campos numéricos con
 * `toDoubleOrNull` / `toIntOrNull`, defaulteando a 0 si vienen vacíos para
 * que el caso de uso devuelva el error correspondiente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomScreen(
    navController: NavController,
    ownerViewModel: OwnerViewModel
) {
    var roomNumber by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(RoomType.SINGLE) }
    var pricePerNight by remember { mutableStateOf("") }
    var maxOccupancy by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val ownerState by ownerViewModel.ownerState.collectAsState()

    val priceValue = pricePerNight.toDoubleOrNull()
    val occupancyValue = maxOccupancy.toIntOrNull()
    val isFormValid = roomNumber.isNotBlank() &&
            description.isNotBlank() &&
            priceValue != null && priceValue > 0.0 &&
            occupancyValue != null && occupancyValue > 0

    LaunchedEffect(ownerState.successMessage) {
        if (ownerState.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            ownerViewModel.clearMessages()
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Agregar Habitación",
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
                        Text("← Cancelar", color = RustOrange)
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Nueva Habitación",
                        fontFamily = PlayfairDisplay,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )

                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Número de Habitación") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RustOrange,
                            focusedLabelColor = RustOrange,
                            cursorColor = RustOrange
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Habitación") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RustOrange,
                                focusedLabelColor = RustOrange
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            RoomType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = pricePerNight,
                        onValueChange = { pricePerNight = it },
                        label = { Text("Precio por Noche (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RustOrange,
                            focusedLabelColor = RustOrange,
                            cursorColor = RustOrange
                        )
                    )

                    OutlinedTextField(
                        value = maxOccupancy,
                        onValueChange = { maxOccupancy = it },
                        label = { Text("Capacidad Máxima") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RustOrange,
                            focusedLabelColor = RustOrange,
                            cursorColor = RustOrange
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RustOrange,
                            focusedLabelColor = RustOrange,
                            cursorColor = RustOrange
                        )
                    )

                    if (ownerState.error != null) {
                        Text(
                            ownerState.error!!,
                            color = ErrorRed,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            ownerViewModel.addRoom(
                                roomNumber = roomNumber,
                                type = selectedType,
                                pricePerNight = priceValue ?: 0.0,
                                maxOccupancy = occupancyValue ?: 0,
                                description = description
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RustOrange
                        ),
                        enabled = isFormValid && !ownerState.isLoading && ownerState.successMessage == null
                    ) {
                        if (ownerState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Agregar Habitación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}