package com.aojeda.jetpackstayrooms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aojeda.jetpackstayrooms.domain.User
import com.aojeda.jetpackstayrooms.domain.usecase.LoginUserUseCase
import com.aojeda.jetpackstayrooms.domain.usecase.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado observable de la sesión y los formularios de autenticación.
 *
 * @property currentUser Usuario autenticado actualmente, o `null` si no hay sesión.
 * @property error Mensaje del último fallo de login/registro listo para
 *  mostrarse al usuario; `null` cuando no hay error que mostrar.
 * @property isLoginSuccess Bandera de un solo uso: se pone a `true` tras un
 *  login exitoso para que la UI dispare la navegación, y la UI debe llamar
 *  después a [AuthViewModel.clearSuccessFlags] para evitar disparos repetidos.
 * @property isRegisterSuccess Equivalente a [isLoginSuccess] para el registro.
 */
data class AuthState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false
)

/**
 * Mantiene la sesión del usuario y orquesta login y registro.
 *
 * Centraliza el estado en un único [AuthState] expuesto como [StateFlow], que
 * todas las pantallas observan a la vez. Los casos de uso ya devuelven [Result],
 * así que aquí solo se traduce su resultado a actualizaciones de estado.
 */
class AuthViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Lanza el login en `viewModelScope` y refleja el resultado en [authState].
     *
     * En éxito establece `currentUser` y activa [AuthState.isLoginSuccess]; en
     * fallo, escribe el mensaje en [AuthState.error] sin modificar el usuario
     * actual.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val result = loginUserUseCase(username, password)

            result.fold(
                onSuccess = { user ->
                    _authState.value = _authState.value.copy(
                        currentUser = user,
                        isLoading = false,
                        isLoginSuccess = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message,
                        isLoginSuccess = false
                    )
                }
            )
        }
    }

    /**
     * Registra un nuevo cliente y, en éxito, lo deja como usuario autenticado
     * activando [AuthState.isRegisterSuccess] para que la UI navegue al listado.
     */
    fun register(username: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val result = registerUserUseCase(username, password, name)

            result.fold(
                onSuccess = { user ->
                    _authState.value = _authState.value.copy(
                        currentUser = user,
                        isLoading = false,
                        isRegisterSuccess = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = exception.message,
                        isRegisterSuccess = false
                    )
                }
            )
        }
    }

    /** Cierra la sesión reiniciando [authState] a sus valores por defecto. */
    fun logout() {
        _authState.value = AuthState()
    }

    /** Borra el mensaje de error tras haberlo mostrado para que no reaparezca. */
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    /**
     * Reinicia las banderas de éxito tras consumirlas. La UI debe llamarla
     * inmediatamente después de reaccionar a un login/registro correcto para
     * evitar que un `LaunchedEffect` se vuelva a disparar.
     */
    fun clearSuccessFlags() {
        _authState.value = _authState.value.copy(
            isLoginSuccess = false,
            isRegisterSuccess = false
        )
    }
}