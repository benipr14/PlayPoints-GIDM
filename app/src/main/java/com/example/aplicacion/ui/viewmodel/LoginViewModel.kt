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

data class LoginUiState(
    val nombre: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent = _loginSuccessEvent.asSharedFlow()

    fun onNombreChanged(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun login() {
        val current = _uiState.value
        if (current.nombre.isBlank() || current.password.isBlank()) {
            _uiState.value = current.copy(error = "Rellena nombre y contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, error = null)
            try {
                val usuarios = buscarUsuarioPorNombreYPasswordSuspend(current.nombre, current.password)
                if (usuarios.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    _loginSuccessEvent.emit(Unit)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Esos datos no pertenecen a ningún usuario"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    private suspend fun buscarUsuarioPorNombreYPasswordSuspend(nombre: String, password: String): List<Usuario> =
        suspendCoroutine { cont ->
            usuarioRepository.buscarUsuarioPorNombreYPassword(nombre, password) { resultado ->
                resultado
                    .onSuccess { usuarios -> cont.resume(usuarios) }
                    .onFailure { error -> cont.resumeWithException(error) }
            }
        }
}

class LoginViewModelFactory(
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(usuarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

