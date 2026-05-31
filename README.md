# JetPackStayRooms

> Aplicación Android nativa de gestión de reservas para un hostal, construida con Kotlin, Jetpack Compose y arquitectura Clean.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Design-757575?logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Room](https://img.shields.io/badge/Room-Database-orange)](https://developer.android.com/training/data-storage/room)
[![Min SDK](https://img.shields.io/badge/minSdk-30-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/11)
[![Target SDK](https://img.shields.io/badge/targetSdk-36-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/14)

---

## Tabla de contenidos

1. [Descripción](#descripción)
2. [Características](#características)
3. [Stack tecnológico](#stack-tecnológico)
4. [Arquitectura](#arquitectura)
5. [Estructura del proyecto](#estructura-del-proyecto)
6. [Modelo de datos](#modelo-de-datos)
7. [Flujo de una operación de extremo a extremo](#flujo-de-una-operación-de-extremo-a-extremo)
8. [Diseño visual](#diseño-visual)
9. [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
10. [Credenciales de demostración](#credenciales-de-demostración)
11. [Decisiones técnicas destacadas](#decisiones-técnicas-destacadas)
12. [Autor](#autor)

---

## Descripción

**JetPackStayRooms** es una aplicación Android que simula el sistema de reservas
de un hostal pequeño. Permite a los **clientes** explorar las habitaciones
disponibles, registrarse, reservar para un rango de fechas y consultar el
historial de sus reservas; y al **propietario** dar de alta nuevas
habitaciones, monitorizar la ocupación y cerrar reservas activas.

El proyecto se ha construido como ejercicio de portfolio para demostrar el
desarrollo Android moderno **end-to-end** sobre una base de código pequeña pero
completa: arquitectura limpia, persistencia con relaciones, navegación
declarativa, gestión de estado reactiva y una UI cuidada con Material 3.

No se utilizan librerías de inyección de dependencias ni de red: toda la
infraestructura está implementada a mano con el SDK de Android, Jetpack y
Kotlin Coroutines, lo que mantiene el código auditable y centrado en los
fundamentos.

---

## Características

### Para clientes

- Registro y login con validación local (campos no vacíos, contraseña mínima
  de 6 caracteres, unicidad de nombre de usuario garantizada por un índice
  único en SQLite). Las contraseñas se almacenan **hasheadas con PBKDF2-HMAC-SHA256**
  y un salt aleatorio por usuario — el dominio nunca ve la contraseña en
  claro fuera de la propia llamada de registro/login.
- Listado de habitaciones disponibles con precio, descripción y tipo.
- Detalle de habitación con selector de fechas (Material 3 `DatePicker`).
- Cálculo automático del precio total (`noches × precio/noche`).
- Pantalla "Mis Reservas" con estados visuales (Activa / Completada /
  Cancelada) y cancelación de reservas activas.

### Para el propietario

- Login con credenciales preconfiguradas durante la siembra de la base de
  datos.
- Panel con estadísticas en vivo: total de habitaciones, libres y ocupadas.
- Listado completo de todas las reservas del sistema (de cualquier cliente).
- Alta de nuevas habitaciones con validación de precio y aforo.
- Finalización (check-out) de reservas activas, que libera la habitación
  automáticamente.

### Transversales

- Single Activity con navegación declarativa basada en `sealed class`.
- Estado por pantalla expuesto como `StateFlow` inmutable.
- Persistencia local con Room, semilla de datos en la primera ejecución.
- Tema Material 3 personalizado (paleta cálida, tipografía mixta
  Serif / Sans-Serif).

---

## Stack tecnológico

| Categoría        | Tecnología                                                          |
| ---------------- | ------------------------------------------------------------------- |
| Lenguaje         | Kotlin 1.9 con corrutinas y `Flow`                                  |
| UI               | Jetpack Compose (BOM), Material 3                                   |
| Navegación       | `androidx.navigation:navigation-compose` 2.7.3                      |
| Estado           | `ViewModel` + `StateFlow` (`androidx.lifecycle` 2.6.2)              |
| Persistencia     | Room (`runtime` + `ktx`), compilado vía KSP                         |
| Concurrencia     | `kotlinx.coroutines` (`viewModelScope`, `Dispatchers.IO`)           |
| Seguridad        | PBKDF2-HMAC-SHA256 (`javax.crypto`) para contraseñas                |
| DI               | Manual — `ViewModelFactory` como *composition root*                 |
| Build            | Gradle Kotlin DSL (`build.gradle.kts`), version catalog (`libs`)    |
| SDK              | `minSdk = 30`, `targetSdk = 36`, `compileSdk = 36`                  |
| JVM target       | 11                                                                  |

---

## Arquitectura

El proyecto sigue **Clean Architecture** con tres capas concéntricas. Las
dependencias siempre apuntan hacia el dominio, nunca al revés:

```
        ┌──────────────────────────────────────────┐
        │                  UI                      │
        │   Composables · Screens · ViewModels     │
        └─────────────────┬────────────────────────┘
                          │ depende de
                          ▼
        ┌──────────────────────────────────────────┐
        │                 DOMAIN                   │
        │   Entities · Repository (interfaces) ·   │
        │              Use Cases                   │
        └─────────────────▲────────────────────────┘
                          │ implementa
        ┌─────────────────┴────────────────────────┐
        │                  DATA                    │
        │   Room (Entities · DAOs · Database) ·    │
        │           Repository Impls               │
        └──────────────────────────────────────────┘
```

### Capa **Domain** (`com.example.jetpackstayrooms.domain`)

Núcleo de negocio puro, sin dependencias de Android ni de Room.

- **Entidades**: `User`, `Room`, `Booking`, `BookingWithDetails`, más los
  enums `RoomType` y `BookingStatus`.
- **Puertos**: interfaces `UserRepository`, `RoomRepository`,
  `BookingRepository` que la capa de datos implementa.
- **Casos de uso**: 9 clases con un único `operator fun invoke(...)` cada una.
  Encapsulan las reglas de negocio (validaciones, cálculo de precio,
  transiciones de estado de reservas) y devuelven `Result<T>` para que la UI
  trate los fallos sin propagar excepciones.

| Caso de uso                | Responsabilidad                                                       |
| -------------------------- | --------------------------------------------------------------------- |
| `RegisterUserUseCase`      | Valida unicidad y campos antes de crear un cliente                    |
| `LoginUserUseCase`         | Autentica credenciales                                                |
| `GetAvailableRoomsUseCase` | Flujo reactivo de habitaciones libres                                 |
| `CreateBookingUseCase`     | Valida fechas, calcula precio y bloquea la habitación                 |
| `CancelBookingUseCase`     | Cancela una reserva activa y libera la habitación                     |
| `CompleteBookingUseCase`   | Cierra (check-out) una reserva activa                                 |
| `GetUserBookingsUseCase`   | Reservas del usuario hidratadas                                       |
| `GetAllBookingsUseCase`    | Reservas de todo el sistema (vista propietario)                       |
| `AddRoomUseCase`           | Da de alta una nueva habitación con validaciones                      |

### Capa **Data** (`com.example.jetpackstayrooms.data`)

Implementación de persistencia con Room.

- **Entidades Room** (`UserEntity`, `RoomEntity`, `BookingEntity`) con
  índices, claves foráneas en cascada y mappers `toDomain()` / `toEntity()`
  como funciones de extensión.
- **`BookingWithDetailsEntity`** combina `@Embedded` + `@Relation` para
  resolver la reserva junto con su habitación y su usuario en una sola
  consulta.
- **DAOs** que exponen `suspend fun` para operaciones puntuales y `Flow`
  para flujos reactivos.
- **`AppDatabase`** singleton con doble verificación, callback de creación
  que pre-puebla la base de datos con un propietario y cuatro habitaciones
  de ejemplo.
- **Repository Impls** que delegan en los DAO y aplican los mappers para que
  el dominio nunca vea entidades de Room.

### Capa **UI** (`com.example.jetpackstayrooms.ui`)

- **`MainActivity`** monta el árbol Compose y construye `ViewModelFactory`.
- **`AppNavigation`** define el `NavHost` y resuelve los ViewModels en el
  *scope* del grafo, compartiéndolos entre pantallas.
- **`Screen`** (sealed class) cataloga las rutas y expone `createRoute` para
  parametrizar URLs sin concatenar cadenas en cada *call site*.
- **ViewModels** exponen un único `data class` de estado por pantalla
  (`AuthState`, `RoomState`, `BookingState`, `OwnerState`) como `StateFlow`.
- **Composables** consumen el estado vía `collectAsState()` y delegan toda
  la lógica en los ViewModels.

### Capa **DI** (`com.example.jetpackstayrooms.di`)

`ViewModelFactory` actúa como *composition root*: instancia base de datos,
repositorios, casos de uso y ViewModels usando `by lazy` para garantizar que
cada dependencia se crea una sola vez y solo cuando hace falta.

---

## Estructura del proyecto

```
app/src/main/java/com/example/jetpackstayrooms/
├── MainActivity.kt
├── domain/
│   ├── User.kt · Room.kt · Booking.kt · BookingWithDetails.kt
│   ├── repository/
│   │   ├── UserRepository.kt
│   │   ├── RoomRepository.kt
│   │   └── BookingRepository.kt
│   └── usecase/
│       ├── RegisterUserUseCase.kt   · LoginUserUseCase.kt
│       ├── GetAvailableRoomsUseCase.kt
│       ├── CreateBookingUseCase.kt  · CancelBookingUseCase.kt
│       ├── CompleteBookingUseCase.kt
│       ├── GetUserBookingsUseCase.kt
│       ├── GetAllBookingsUseCase.kt
│       └── AddRoomUseCase.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/        (UserDao, RoomDao, BookingDao)
│   │   └── entity/     (UserEntity, RoomEntity, BookingEntity,
│   │                    BookingWithDetailsEntity)
│   ├── repository/     (UserRepositoryImpl, RoomRepositoryImpl,
│   │                    BookingRepositoryImpl)
│   └── security/
│       └── PasswordHasher.kt   (PBKDF2-HMAC-SHA256)
├── di/
│   └── ViewModelFactory.kt
└── ui/
    ├── navigation/     (Screen.kt, AppNavigation.kt)
    ├── viewmodel/      (AuthViewModel, RoomViewModel,
    │                    BookingViewModel, OwnerViewModel)
    ├── screen/         (LoginScreen, RegisterScreen,
    │                    RoomListScreen, RoomDetailScreen,
    │                    BookingListScreen,
    │                    OwnerDashboardScreen, AddRoomScreen)
    └── theme/          (Color.kt, Type.kt, Theme.kt)
```

---

## Modelo de datos

Tres tablas con dos claves foráneas en cascada:

```
┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│    users     │        │   bookings   │        │    rooms     │
├──────────────┤        ├──────────────┤        ├──────────────┤
│ id (PK)      │◄───────┤ userId (FK)  │   ┌────┤ id (PK)      │
│ username (U) │        │ id (PK)      │   │    │ roomNumber   │
│ passwordHash │        │ roomId (FK)  │───┘    │ type         │
│ salt         │        │ checkInDate  │        │ pricePerNight│
│ name         │        │ checkOutDate │        │ maxOccupancy │
│ isOwner      │        │ status       │        │ description  │
└──────────────┘        │ totalPrice   │        │ isAvailable  │
                        └──────────────┘        └──────────────┘
                              ON DELETE CASCADE en ambas FK
```

**Decisiones de modelado relevantes:**

- `username` tiene un índice único: la unicidad la garantiza SQLite, no la
  aplicación, evitando *race conditions*.
- Las contraseñas no se guardan en claro: solo el `passwordHash` (PBKDF2,
  120 000 iteraciones) y un `salt` de 16 bytes generados por usuario. La
  comparación durante el login se hace en tiempo constante para mitigar
  *timing attacks*.
- Las fechas se almacenan como `String` en formato ISO-8601 para evitar
  registrar un `TypeConverter` para `LocalDate`; los mappers se encargan
  del parseo.
- Los enums (`RoomType`, `BookingStatus`) se persisten por su `name`, no
  por ordinal: añadir un nuevo valor en medio del enum no rompe los datos
  ya guardados.
- `bookings` define índices secundarios sobre `userId` y `roomId` para que
  los `JOIN` y los borrados en cascada no requieran escaneos completos.
- La disponibilidad (`isAvailable`) se trata como **cache derivada del
  estado real de las reservas**: cancelar o finalizar una reserva solo
  libera la habitación si no quedan otras reservas activas sobre ella.

---

## Flujo de una operación de extremo a extremo

Crear una reserva involucra las tres capas y muestra cómo se comunican entre
sí:

```
┌─────────────────────────┐
│ RoomDetailScreen        │  Composable lee fechas, recoge userId
│  └─ Button("Reservar")  │  de la sesión y llama al ViewModel
└──────────┬──────────────┘
           │ bookingViewModel.createBooking(...)
           ▼
┌─────────────────────────┐
│ BookingViewModel        │  Lanza en viewModelScope, traslada el
│                         │  Result a BookingState (snackbar/navegación)
└──────────┬──────────────┘
           │ createBookingUseCase(userId, roomId, in, out)
           ▼
┌─────────────────────────┐
│ CreateBookingUseCase    │  Valida fechas, calcula precio y delega en
│                         │  una operación atómica
└──────────┬──────────────┘
           │ bookingRepository.insertBookingAtomically(...)
           ▼
┌─────────────────────────┐
│ BookingRepositoryImpl   │  Mapea Booking -> BookingEntity y delega
└──────────┬──────────────┘
           ▼
┌─────────────────────────┐
│ BookingDao              │  @Transaction:
│                         │   1. UPDATE rooms SET isAvailable = 0
│                         │      WHERE id = :roomId AND isAvailable = 1
│                         │   2. Si afectó 1 fila → INSERT booking
│                         │      Si afectó 0 filas → devuelve -1
└──────────┬──────────────┘
           ▼
┌─────────────────────────┐
│ Room (SQLite)           │  Garantiza la atomicidad y serializa
│                         │  reservas concurrentes sobre la misma fila
└─────────────────────────┘
```

La actualización en la pantalla principal (`RoomListScreen`) ocurre
**automáticamente**: el `Flow` que devuelve `getAvailableRooms()` reemite la
lista filtrada al detectar el cambio de columna `isAvailable`, sin que la UI
tenga que invalidar caché ni recargar.

---

## Diseño visual

Tema Material 3 personalizado, exclusivamente claro, basado en una paleta
cálida y elegante (referencia: arquitectura mediterránea / interiores
boutique):

| Token         | Hex       | Uso                                              |
| ------------- | --------- | ------------------------------------------------ |
| `DeepNavy`    | `#1A2332` | Texto principal y títulos                        |
| `RustOrange`  | `#D17842` | Color primario, llamadas a la acción             |
| `SoftCream`   | `#FAF6F1` | Fondos y *top bars*                              |
| `LightSage`   | `#B8C5B0` | Secundario, fondos suaves                        |
| `WarmTaupe`   | `#9B8B7E` | Bordes y separadores                             |
| `AccentGold`  | `#CDA35F` | Color terciario                                  |
| `SuccessGreen`| `#6B9B37` | Estado "Activa" y feedback positivo              |
| `ErrorRed`    | `#D84315` | Estado "Cancelada" y mensajes de error           |

La tipografía combina **`FontFamily.Serif`** para titulares (`Playfair
Display` conceptualmente, mapeado al serif del sistema para evitar añadir
fuentes a `res/font`) y **`FontFamily.SansSerif`** para cuerpo y etiquetas.

---

## Cómo ejecutar el proyecto

### Requisitos

- **Android Studio** Iguana (2023.2.1) o superior, con el Android Gradle
  Plugin compatible con `compileSdk = 36`.
- **JDK 11**.
- Un emulador o dispositivo físico con **Android 11 (API 30)** o superior.

### Pasos

```bash
git clone https://github.com/AOjeda006/JetPackStayRooms.git
cd JetPackStayRooms
```

Abre el proyecto en Android Studio, deja que Gradle sincronice y ejecuta la
configuración `app` sobre un emulador o dispositivo.

Alternativamente, desde línea de comandos:

```bash
./gradlew :app:installDebug
```

La primera vez que la app arranca, Room crea la base de datos y dispara
`DatabaseCallback`, que inserta el usuario propietario y cuatro habitaciones
de ejemplo.

---

## Credenciales de demostración

| Rol         | Usuario | Contraseña | Acceso                                 |
| ----------- | ------- | ---------- | -------------------------------------- |
| Propietario | `owner` | `owner123` | Panel del propietario y gestión global |
| Cliente     | —       | —          | Crea uno desde la pantalla de registro |

> Las credenciales del propietario se siembran en
> `AppDatabase.populateDatabase()` y están pensadas únicamente para pruebas
> locales.

---

## Decisiones técnicas destacadas

- **Sin librerías de DI**: el cableado se hace en `ViewModelFactory` con
  `by lazy`. El proyecto demuestra que puede entenderse el *ciclo de vida
  de las dependencias* sin esconderlo detrás de Hilt o Koin.
- **`Result<T>` en los casos de uso** en lugar de lanzar excepciones: la
  capa de presentación se limita a hacer `result.fold(...)` y proyectar el
  mensaje al estado, lo que mantiene la UI predecible y testable.
- **`Flow` end-to-end para listados**: los `LiveData` no aparecen en
  ninguna parte; toda la reactividad se basa en `StateFlow` y `Flow` de
  Room, alineada con Compose vía `collectAsState`.
- **`Single Activity` + `sealed class Screen`**: una sola Activity hospeda
  todo el grafo de navegación, y los destinos se centralizan en una
  jerarquía cerrada con helpers de parametrización para evitar literales
  duplicados.
- **Reservas concurrentes seguras**: el `BookingDao` expone
  `insertBookingAtomically`, un método anotado con `@Transaction` que
  combina un compare-and-set sobre `rooms.isAvailable` con el `INSERT` de
  la reserva. Dos clientes que pulsen "Reservar" a la vez se serializan en
  SQLite y solo uno obtiene la habitación, sin necesidad de bloqueos
  optimistas a nivel de aplicación.
- **Disponibilidad derivada**: `isAvailable` se trata como una cache, no
  como una verdad autónoma. Al cancelar o finalizar una reserva se cuenta
  cuántas reservas activas quedan en esa habitación y solo se libera si la
  cuenta es cero — la habitación nunca se libera "de más".
- **Contraseñas hasheadas**: PBKDF2-HMAC-SHA256 con 120 000 iteraciones y
  salt aleatorio de 16 bytes por usuario; comparación de hashes en tiempo
  constante. El módulo `data/security/PasswordHasher.kt` es el único
  lugar del proyecto que conoce la primitiva de cifrado.
- **Coroutines sin jobs zombi**: los ViewModels guardan el `Job` de cada
  `collect` y lo cancelan antes de relanzar, evitando que llamadas
  repetidas (logout, cambio de usuario) acumulen colectores escribiendo en
  paralelo sobre el mismo `StateFlow`.
- **Pre-poblamiento defensivo**: el callback `onCreate` se lanza con
  `SupervisorJob` + `CoroutineExceptionHandler` para que cualquier fallo
  de siembra quede registrado en logcat en lugar de perderse.
- **`@Transaction` en consultas con `@Relation`**: garantiza que la lectura
  del padre y de sus hijos (`booking + room + user`) ocurra dentro de la
  misma transacción y sea consistente.

---

## Autor

**Andrés Ojeda Rodríguez**
[andresojedarodriguez@gmail.com](mailto:andresojedarodriguez@gmail.com)

---

## Licencia

Este proyecto está licenciado bajo la **PolyForm Noncommercial License 1.0.0**.
Puedes ver, ejecutar, estudiar y modificar el código con fines **no comerciales**
(estudio personal, educación, evaluación), pero **cualquier uso comercial requiere
permiso escrito del autor**. Consulta el archivo [LICENSE.md](LICENSE.md) para los
términos completos.

© 2026 Andrés Ojeda Rodríguez. Todos los derechos no concedidos expresamente quedan reservados.
