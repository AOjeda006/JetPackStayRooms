package com.aojeda.jetpackstayrooms.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aojeda.jetpackstayrooms.domain.Room
import com.aojeda.jetpackstayrooms.ui.navigation.Screen
import com.aojeda.jetpackstayrooms.ui.theme.*
import com.aojeda.jetpackstayrooms.ui.viewmodel.AuthViewModel
import com.aojeda.jetpackstayrooms.ui.viewmodel.RoomViewModel

/**
 * Pantalla principal: listado de habitaciones disponibles.
 *
 * Hace de *landing*: muestra las habitaciones tanto si hay sesión iniciada
 * como si no. La barra superior cambia según el estado de [AuthViewModel]:
 *
 * - Sin sesión: ofrece "Iniciar Sesión".
 * - Con sesión cliente: enlaza a "Mis Reservas".
 * - Con sesión propietario: enlaza al "Dashboard".
 *
 * Cuando no hay habitaciones disponibles muestra el cartel "NO VACANCY" en
 * lugar de una lista vacía.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    navController: NavController,
    roomViewModel: RoomViewModel,
    authViewModel: AuthViewModel
) {
    val roomState by roomViewModel.roomState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "JetPack Stay Rooms",
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = DeepNavy
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SoftCream
                ),
                actions = {
                    if (authState.currentUser != null) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                authState.currentUser!!.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DeepNavy
                            )
                            Row {
                                TextButton(onClick = {
                                    if (authState.currentUser!!.isOwner) {
                                        navController.navigate(Screen.OwnerDashboard.route)
                                    } else {
                                        navController.navigate(Screen.BookingList.route)
                                    }
                                }) {
                                    Text(
                                        if (authState.currentUser!!.isOwner) "Dashboard" else "Reservas",
                                        fontSize = 11.sp,
                                        color = RustOrange
                                    )
                                }
                                TextButton(onClick = {
                                    authViewModel.logout()
                                    roomViewModel.loadAvailableRooms()
                                }) {
                                    Text("Salir", fontSize = 11.sp, color = RustOrange)
                                }
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { navController.navigate(Screen.Login.route) },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Iniciar Sesión", color = RustOrange, fontWeight = FontWeight.Medium)
                        }
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
                roomState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = RustOrange
                    )
                }
                roomState.rooms.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "NO VACANCY",
                            fontFamily = PlayfairDisplay,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy.copy(alpha = 0.3f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(roomState.rooms) { room ->
                            RoomCard(
                                room = room,
                                onClick = {
                                    navController.navigate(Screen.RoomDetail.createRoute(room.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta resumen de una habitación dentro del listado.
 *
 * @param onClick Callback que la pantalla padre usa para navegar al detalle.
 */
@Composable
fun RoomCard(
    room: Room,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Habitación ${room.roomNumber}",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DeepNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    room.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontFamily = Montserrat,
                    fontSize = 14.sp,
                    color = RustOrange,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    room.description,
                    fontSize = 13.sp,
                    color = DeepNavy.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${room.pricePerNight.toInt()}€",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = RustOrange
                )
                Text(
                    "por noche",
                    fontSize = 11.sp,
                    color = DeepNavy.copy(alpha = 0.5f)
                )
            }
        }
    }
}