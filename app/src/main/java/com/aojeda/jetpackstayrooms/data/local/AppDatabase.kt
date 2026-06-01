package com.aojeda.jetpackstayrooms.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aojeda.jetpackstayrooms.data.local.dao.BookingDao
import com.aojeda.jetpackstayrooms.data.local.dao.RoomDao
import com.aojeda.jetpackstayrooms.data.local.dao.UserDao
import com.aojeda.jetpackstayrooms.data.local.entity.BookingEntity
import com.aojeda.jetpackstayrooms.data.local.entity.RoomEntity
import com.aojeda.jetpackstayrooms.data.local.entity.UserEntity
import com.aojeda.jetpackstayrooms.data.security.PasswordHasher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Base de datos Room de la aplicación. Agrupa las tres entidades persistibles y
 * expone los DAO que la capa de datos usa para acceder a ellas.
 *
 * Se accede a la instancia mediante [getDatabase], que aplica el patrón
 * singleton con doble verificación para que solo exista una conexión por
 * proceso.
 */
@Database(
    entities = [UserEntity::class, RoomEntity::class, BookingEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Devuelve la instancia compartida creando la base de datos en la primera
         * llamada. Es thread-safe: el bloque `synchronized` evita dos creaciones
         * concurrentes y `@Volatile` asegura visibilidad de la instancia entre
         * hilos.
         *
         * Se aplica [fallbackToDestructiveMigration] como estrategia explícita
         * para esta fase del proyecto: cualquier salto de versión que no tenga
         * una `Migration` registrada borra y recrea la base de datos en lugar
         * de hacer crashear la app. Es aceptable porque todos los datos son
         * recuperables (los reales se generan en runtime y el propietario se
         * vuelve a sembrar). En producción real habría que reemplazarlo por
         * migraciones explícitas.
         *
         * En la creación inicial se registra [DatabaseCallback], que pre-puebla
         * la base de datos con un usuario propietario y cuatro habitaciones de
         * ejemplo.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jetpack_stay_rooms_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback que dispara la prepoblación cuando Room crea físicamente las
         * tablas por primera vez. No se ejecuta en aperturas posteriores.
         *
         * La corrutina de siembra se lanza con un [SupervisorJob] y un
         * [CoroutineExceptionHandler] que loggea cualquier fallo: sin él, una
         * excepción en `populateDatabase` se perdería silenciosamente y dejaría
         * la base de datos a medio sembrar sin pistas en logcat.
         */
        private class DatabaseCallback : Callback() {

            private val seedExceptionHandler = CoroutineExceptionHandler { _, error ->
                Log.e("AppDatabase", "Fallo al sembrar la base de datos", error)
            }

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(SupervisorJob() + Dispatchers.IO + seedExceptionHandler).launch {
                        populateDatabase(database)
                    }
                }
            }
        }

        /**
         * Inserta datos semilla: el usuario propietario por defecto
         * (`owner` / `owner123`) y cuatro habitaciones representativas de cada
         * [com.aojeda.jetpackstayrooms.domain.RoomType].
         *
         * La contraseña del propietario se cifra con [PasswordHasher] antes de
         * guardarse, igual que las contraseñas de cualquier otro usuario; el
         * literal `"owner123"` solo existe en este punto del código para que
         * las pruebas locales sigan teniendo unas credenciales conocidas.
         */
        private suspend fun populateDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            val roomDao = database.roomDao()

            // Insertar usuario dueño (contraseña hasheada)
            val ownerSalt = PasswordHasher.newSalt()
            val owner = UserEntity(
                username = "owner",
                passwordHash = PasswordHasher.hash("owner123", ownerSalt),
                salt = ownerSalt,
                name = "Propietario del Hostal",
                isOwner = true
            )
            userDao.insert(owner)

            // Insertar habitaciones de ejemplo
            val rooms = listOf(
                RoomEntity(
                    roomNumber = "101",
                    type = "SINGLE",
                    pricePerNight = 50.0,
                    maxOccupancy = 1,
                    description = "Habitación individual con cama simple, baño privado y TV.",
                    isAvailable = true
                ),
                RoomEntity(
                    roomNumber = "102",
                    type = "DOUBLE",
                    pricePerNight = 75.0,
                    maxOccupancy = 2,
                    description = "Habitación doble con dos camas, baño privado, TV y minibar.",
                    isAvailable = true
                ),
                RoomEntity(
                    roomNumber = "201",
                    type = "SUITE",
                    pricePerNight = 120.0,
                    maxOccupancy = 3,
                    description = "Suite con sala de estar, dormitorio, baño con jacuzzi y balcón.",
                    isAvailable = true
                ),
                RoomEntity(
                    roomNumber = "202",
                    type = "DELUXE",
                    pricePerNight = 150.0,
                    maxOccupancy = 4,
                    description = "Habitación deluxe con vista panorámica, jacuzzi y todas las comodidades.",
                    isAvailable = true
                )
            )

            rooms.forEach { roomDao.insert(it) }
        }
    }
}