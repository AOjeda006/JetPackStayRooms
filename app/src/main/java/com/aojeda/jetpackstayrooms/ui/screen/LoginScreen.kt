package com.aojeda.jetpackstayrooms.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aojeda.jetpackstayrooms.ui.navigation.Screen
import com.aojeda.jetpackstayrooms.ui.theme.*
import com.aojeda.jetpackstayrooms.ui.viewmodel.AuthViewModel

/**
 * Pantalla de inicio de sesión.
 *
 * Observa [AuthViewModel.authState] y reacciona a [AuthState.isLoginSuccess] con
 * un `LaunchedEffect` que navega al listado de habitaciones tras un login
 * exitoso, limpiando antes el back stack para que el botón "atrás" no devuelva
 * al formulario.
 *
 * Muestra como pista las credenciales del propietario sembrado
 * (`owner` / `owner123`) para facilitar pruebas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.isLoginSuccess) {
        if (authState.isLoginSuccess) {
            authViewModel.clearSuccessFlags()
            navController.navigate(Screen.RoomList.route) {
                popUpTo(Screen.RoomList.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Iniciar Sesión",
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
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Bienvenido",
                        fontFamily = PlayfairDisplay,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Inicia sesión para continuar",
                        fontSize = 14.sp,
                        color = DeepNavy.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        "owner // owner123",
                        fontSize = 14.sp,
                        color = DeepNavy.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RustOrange,
                            focusedLabelColor = RustOrange,
                            cursorColor = RustOrange
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RustOrange,
                            focusedLabelColor = RustOrange,
                            cursorColor = RustOrange
                        )
                    )

                    if (authState.error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            authState.error!!,
                            color = ErrorRed,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            authViewModel.login(username, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RustOrange
                        ),
                        enabled = !authState.isLoading
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Iniciar Sesión",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { navController.navigate(Screen.Register.route) }
                    ) {
                        Text(
                            "¿No tienes cuenta? Regístrate",
                            color = RustOrange,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}