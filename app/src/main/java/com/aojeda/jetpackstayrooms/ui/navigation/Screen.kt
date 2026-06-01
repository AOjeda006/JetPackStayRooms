package com.aojeda.jetpackstayrooms.ui.navigation

/**
 * Catálogo cerrado de destinos del grafo de navegación.
 *
 * Cada destino encapsula su ruta. Las rutas parametrizadas como [RoomDetail]
 * exponen un helper `createRoute` para construir la URL con los argumentos
 * resueltos sin tener que concatenar cadenas en cada *call site*.
 *
 * @property route Patrón de ruta que se registra en el `NavHost`; puede contener
 *  *placeholders* `{nombre}` para argumentos.
 */
sealed class Screen(val route: String) {
    data object RoomList : Screen("room_list")
    data object Login : Screen("login")
    data object Register : Screen("register")

    data object RoomDetail : Screen("room_detail/{roomId}") {
        /**
         * Sustituye el placeholder `{roomId}` por el valor real.
         *
         * @return Ruta concreta lista para pasar a `navController.navigate(...)`.
         */
        fun createRoute(roomId: Long) = "room_detail/$roomId"
    }

    data object BookingList : Screen("booking_list")
    data object OwnerDashboard : Screen("owner_dashboard")
    data object AddRoom : Screen("add_room")
}