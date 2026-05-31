package com.example.jetpackstayrooms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.jetpackstayrooms.di.ViewModelFactory
import com.example.jetpackstayrooms.ui.navigation.AppNavigation
import com.example.jetpackstayrooms.ui.theme.JetPackStayRoomsTheme

/**
 * Punto de entrada de la aplicación.
 *
 * Construye el [com.example.jetpackstayrooms.di.ViewModelFactory] una sola vez
 * por ciclo de vida de la Activity, monta el árbol Compose dentro del tema de
 * la app e instala el grafo de navegación raíz.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModelFactory = ViewModelFactory(applicationContext)

        setContent {
            JetPackStayRoomsTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    viewModelFactory = viewModelFactory
                )
            }
        }
    }
}