package com.example.aplicacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aplicacion.data.model.Usuario
import com.example.aplicacion.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class RegistrationUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class RegistrationViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val fechaCreacionProvider: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()

    private val _registeredEvent = MutableSharedFlow<Unit>()
    val registeredEvent = _registeredEvent.asSharedFlow()

    fun onNombreChanged(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value)
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }


    fun register() {
        val current = _uiState.value
        if (current.email.isBlank() || current.nombre.isBlank() || current.password.isBlank()) {
            _uiState.value = current.copy(error = "Rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, error = null)
            try {
                val existentes = buscarUsuarioPorEmailSuspend(current.email)
                if (existentes.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "El usuario ya existe")
                    return@launch
                }

                val usuario = Usuario(
                    nombre = current.nombre,
                    email = current.email,
                    password = current.password,
                    fechaCreacion = fechaCreacionProvider(),
                    puntos = 1000
                )

                guardarUsuarioConIdSecuencialSuspend(usuario)

                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                _registeredEvent.emit(Unit)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "${e.message}")
            }
        }
    }

    private suspend fun buscarUsuarioPorEmailSuspend(email: String): List<Usuario> =
        suspendCoroutine { cont ->
            usuarioRepository.buscarUsuarioPorEmail(email) { resultado ->
                resultado
                    .onSuccess { usuarios -> cont.resume(usuarios) }
                    .onFailure { error -> cont.resumeWithException(error) }
            }
        }

    private suspend fun guardarUsuarioConIdSecuencialSuspend(usuario: Usuario): String =
        suspendCoroutine { cont ->
            usuarioRepository.guardarUsuarioConIdSecuencial(usuario) { resultado ->
                resultado
                    .onSuccess { id -> cont.resume(id) }
                    .onFailure { error -> cont.resumeWithException(error) }
            }
        }
}

class RegistrationViewModelFactory(
    private val usuarioRepository: UsuarioRepository,
    private val fechaCreacionProvider: () -> String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistrationViewModel(usuarioRepository, fechaCreacionProvider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

