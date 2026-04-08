package com.example.aplicacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aplicacion.data.repository.ApuestaRepository
import com.example.aplicacion.data.repository.CompeticionRepository
import com.example.aplicacion.data.model.Usuario
import com.example.aplicacion.data.repository.EquipoRepository
import com.example.aplicacion.data.repository.LigaAmistosaRepository
import com.example.aplicacion.data.repository.LimpiezaFirestoreRepository
import com.example.aplicacion.data.repository.MensajePantallaRepository
import com.example.aplicacion.data.repository.MiembroRepository
import com.example.aplicacion.data.repository.PartidoRepository
import com.example.aplicacion.data.repository.UsuarioRepository
import com.example.aplicacion.ui.theme.Prueba_firebaseTheme
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    private val usuarioRepository = UsuarioRepository()
    private val ligaAmistosaRepository = LigaAmistosaRepository()
    private val apuestaRepository = ApuestaRepository()
    private val competicionRepository = CompeticionRepository()
    private val partidoRepository = PartidoRepository()
    private val equipoRepository = EquipoRepository()
    private val miembroRepository = MiembroRepository()
    private val mensajePantallaRepository = MensajePantallaRepository()
    private val limpiezaFirestoreRepository = LimpiezaFirestoreRepository()
    private var uiMessage by mutableStateOf("Pulsa un botón para continuar")
    private var usuariosLecturaCompletaTexto by mutableStateOf("Lectura completa de usuarios: (sin datos)")
    private var usuariosLecturaFiltradaTexto by mutableStateOf("Lectura filtrada (cooldownActivo=true): (sin datos)")
    private var isLoading by mutableStateOf(false)
    private var ultimoUsuarioId by mutableStateOf<String?>(null)
    private var ultimaLigaId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Prueba_firebaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { ejecutarCasoUsoUsuarios() },
                            enabled = !isLoading
                        ) {
                            Text(text = if (isLoading) "Guardando..." else "Agregar usuario")
                        }

                        Button(
                            onClick = { agregarCooldownDosMinutos() },
                            enabled = !isLoading && ultimoUsuarioId != null
                        ) {
                            Text(text = "Añadir cooldown 2 min")
                        }

                        Button(
                            onClick = {
                                runCatching { ejecutarCasoUsoLigasAmistosas() }
                                    .onFailure { error -> uiMessage = "Error: ${error.message}" }
                            },
                            enabled = !isLoading
                        ) {
                            Text(text = "Agregar liga amistosa")
                        }

                        Button(
                            onClick = {
                                runCatching { ejecutarCasoUsoApuestas() }
                                    .onFailure { error -> uiMessage = "Error: ${error.message}" }
                            },
                            enabled = !isLoading && ultimoUsuarioId != null
                        ) {
                            Text(text = "Agregar apuesta")
                        }

                        Button(
                            onClick = {
                                runCatching { ejecutarCasoUsoCompeticiones() }
                                    .onFailure { error -> uiMessage = "Error: ${error.message}" }
                            },
                            enabled = !isLoading
                        ) {
                            Text(text = "Agregar competición")
                        }

                        Button(
                            onClick = {
                                runCatching { ejecutarCasoUsoPartidos() }
                                    .onFailure { error -> uiMessage = "Error: ${error.message}" }
                            },
                            enabled = !isLoading
                        ) {
                            Text(text = "Agregar partido")
                        }

                        Button(
                            onClick = {
                                runCatching { ejecutarCasoUsoEquipos() }
                                    .onFailure { error -> uiMessage = "Error: ${error.message}" }
                            },
                            enabled = !isLoading
                        ) {
                            Text(text = "Agregar equipo")
                        }

                        Button(
                            onClick = {
                                runCatching { ejecutarCasoUsoMiembros() }
                                    .onFailure { error -> uiMessage = "Error: ${error.message}" }
                            },
                            enabled = !isLoading
                        ) {
                            Text(text = "Agregar miembro")
                        }

                        Button(
                            onClick = {
                                runCatching { ejecutarCasoUsoMensajesPantalla() }
                                    .onFailure { error -> uiMessage = "Error: ${error.message}" }
                            },
                            enabled = !isLoading
                        ) {
                            Text(text = "Agregar mensaje pantalla")
                        }

                        Button(
                            onClick = {
                                runCatching { ejecutarCasoUsoVaciarBaseDatos() }
                                    .onFailure { error -> uiMessage = "Error: ${error.message}" }
                            },
                            enabled = !isLoading
                        ) {
                            Text(text = "Vaciar base de datos")
                        }

                        Text(text = usuariosLecturaCompletaTexto)
                        Text(text = usuariosLecturaFiltradaTexto)
                        Text(text = uiMessage)
                    }
                }
            }
        }
    }

    private fun fechaActualTexto(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS z", Locale.forLanguageTag("es-ES"))
        return formatter.format(Date())
    }

    private fun fechaEnDosMinutosTexto(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS z", Locale.forLanguageTag("es-ES"))
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.MINUTE, 2)
        return formatter.format(calendario.time)
    }

    private fun fechaEnUnaSemanaTexto(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS z", Locale.forLanguageTag("es-ES"))
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.WEEK_OF_YEAR, 1)
        return formatter.format(calendario.time)
    }

    private fun fechaActualIsoUtcTexto(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }

    private fun ejecutarCasoUsoUsuarios() {
        isLoading = true
        val fechaCreacionActual = fechaActualTexto()

        val usuarioEjemplo = Usuario(
            nombre = "beni_14",
            email = "benigno.parra@gmail.com",
            role = "user",
            fechaCreacion = fechaCreacionActual,
            cooldownHasta = "6 de abril de 2026 a las 6:14:47 p.m. UTC+2",
            cooldownActivo = false,
            puntos = 1200
        )

        usuarioRepository.guardarUsuarioConIdSecuencial(usuarioEjemplo) { escritura ->
            escritura
                .onSuccess { documentIdGenerado ->
                    ultimoUsuarioId = documentIdGenerado
                    usuarioRepository.obtenerTodosLosUsuarios { lecturaCompleta ->
                        lecturaCompleta
                            .onSuccess { usuariosCompletos ->
                                usuariosLecturaCompletaTexto = construirResumenUsuarios(
                                    titulo = "Lectura completa de usuarios",
                                    usuarios = usuariosCompletos
                                )
                                usuarioRepository.obtenerUsuariosConCooldownActivo(true) { lecturaFiltrada ->
                                    lecturaFiltrada
                                        .onSuccess { usuariosFiltrados ->
                                            usuariosLecturaFiltradaTexto = construirResumenUsuarios(
                                                titulo = "Lectura filtrada (cooldownActivo=true)",
                                                usuarios = usuariosFiltrados
                                            )
                                            isLoading = false
                                            uiMessage = "Usuario agregado correctamente: $documentIdGenerado"
                                        }
                                        .onFailure { error ->
                                            usuariosLecturaFiltradaTexto =
                                                "Lectura filtrada (cooldownActivo=true): error -> ${error.message}"
                                            isLoading = false
                                            uiMessage = "Error: ${error.message}"
                                        }
                                }
                            }
                            .onFailure { error ->
                                usuariosLecturaCompletaTexto =
                                    "Lectura completa de usuarios: error -> ${error.message}"
                                isLoading = false
                                uiMessage = "Error: ${error.message}"
                            }
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }

    private fun construirResumenUsuarios(titulo: String, usuarios: List<Usuario>): String {
        if (usuarios.isEmpty()) {
            return "$titulo: 0 usuarios"
        }

        val detalle = usuarios.joinToString(separator = " | ") { usuario ->
            val idVisible = usuario.usuarioId.ifBlank { usuario.id }
            "$idVisible (${usuario.nombre}, puntos=${usuario.puntos}, cooldown=${usuario.cooldownActivo})"
        }

        return "$titulo: ${usuarios.size} usuarios -> $detalle"
    }

    private fun agregarCooldownDosMinutos() {
        val usuarioId = ultimoUsuarioId
        if (usuarioId == null) {
            uiMessage = "Primero agrega un usuario"
            return
        }

        isLoading = true
        val cooldownHasta = fechaEnDosMinutosTexto()

        usuarioRepository.actualizarCooldownUsuario(usuarioId, cooldownHasta) { resultado ->
            resultado
                .onSuccess {
                    usuarioRepository.obtenerTodosLosUsuarios { lecturaCompleta ->
                        lecturaCompleta
                            .onSuccess { usuariosCompletos ->
                                usuariosLecturaCompletaTexto = construirResumenUsuarios(
                                    titulo = "Lectura completa de usuarios",
                                    usuarios = usuariosCompletos
                                )

                                usuarioRepository.obtenerUsuariosConCooldownActivo(true) { lecturaFiltrada ->
                                    lecturaFiltrada
                                        .onSuccess { usuariosFiltrados ->
                                            usuariosLecturaFiltradaTexto = construirResumenUsuarios(
                                                titulo = "Lectura filtrada (cooldownActivo=true)",
                                                usuarios = usuariosFiltrados
                                            )
                                            isLoading = false
                                            uiMessage = "Cooldown aplicado correctamente a $usuarioId"
                                        }
                                        .onFailure { error ->
                                            usuariosLecturaFiltradaTexto =
                                                "Lectura filtrada (cooldownActivo=true): error -> ${error.message}"
                                            isLoading = false
                                            uiMessage = "Error: ${error.message}"
                                        }
                                }
                            }
                            .onFailure { error ->
                                usuariosLecturaCompletaTexto =
                                    "Lectura completa de usuarios: error -> ${error.message}"
                                isLoading = false
                                uiMessage = "Error: ${error.message}"
                            }
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }

    private fun ejecutarCasoUsoLigasAmistosas() {
        isLoading = true

        runCatching {
            val creadoPor = ultimoUsuarioId ?: "usuario1"
            val creadoEn = fechaActualTexto()

            ligaAmistosaRepository.guardarLigaConIdSecuencial(
                nombre = "Liga de amigos",
                creadoPor = creadoPor,
                creadoEn = creadoEn,
                numMiembros = 4
            ) { escritura ->
                escritura
                    .onSuccess { ligaGuardada ->
                        ultimaLigaId = ligaGuardada.ligaId.ifBlank { ligaGuardada.id }
                        ligaAmistosaRepository.obtenerTodasLasLigas { lecturaCompleta ->
                            lecturaCompleta
                                .onSuccess {
                                    ligaAmistosaRepository.obtenerLigasNoFinalizadas { lecturaFiltrada ->
                                        lecturaFiltrada
                                            .onSuccess {
                                                isLoading = false
                                                uiMessage = "Liga creada correctamente: ${ligaGuardada.id} (${ligaGuardada.codigo})"
                                            }
                                            .onFailure { error ->
                                                isLoading = false
                                                uiMessage = "Error: ${error.message}"
                                            }
                                    }
                                }
                                .onFailure { error ->
                                    isLoading = false
                                    uiMessage = "Error: ${error.message}"
                                }
                        }
                    }
                    .onFailure { error ->
                        isLoading = false
                        uiMessage = "Error: ${error.message}"
                    }
            }
        }.onFailure { error ->
            isLoading = false
            uiMessage = "Error: ${error.message}"
        }
    }

    private fun ejecutarCasoUsoApuestas() {
        val usuarioId = ultimoUsuarioId
        if (usuarioId == null) {
            uiMessage = "Primero agrega un usuario"
            return
        }

        isLoading = true
        val fechaApuesta = fechaActualTexto()

        apuestaRepository.guardarApuestaConIdSecuencial(usuarioId, fechaApuesta) { escritura ->
            escritura
                .onSuccess { apuestaGuardada ->
                    apuestaRepository.obtenerTodasLasApuestas(usuarioId) { lecturaCompleta ->
                        lecturaCompleta
                            .onSuccess {
                                apuestaRepository.obtenerApuestasPendientes(usuarioId) { lecturaFiltrada ->
                                    lecturaFiltrada
                                        .onSuccess {
                                            isLoading = false
                                            uiMessage = "Apuesta creada correctamente: ${apuestaGuardada.id}"
                                        }
                                        .onFailure { error ->
                                            isLoading = false
                                            uiMessage = "Error: ${error.message}"
                                        }
                                }
                            }
                            .onFailure { error ->
                                isLoading = false
                                uiMessage = "Error: ${error.message}"
                            }
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }

    private fun ejecutarCasoUsoCompeticiones() {
        isLoading = true

        competicionRepository.guardarCompeticionConIdSecuencial { escritura ->
            escritura
                .onSuccess { competicionGuardada ->
                    competicionRepository.obtenerTodasLasCompeticiones { lecturaCompleta ->
                        lecturaCompleta
                            .onSuccess {
                                competicionRepository.obtenerCompeticionesPorPais("España") { lecturaFiltrada ->
                                    lecturaFiltrada
                                        .onSuccess {
                                            isLoading = false
                                            uiMessage = "Competición creada correctamente: ${competicionGuardada.id}"
                                        }
                                        .onFailure { error ->
                                            isLoading = false
                                            uiMessage = "Error: ${error.message}"
                                        }
                                }
                            }
                            .onFailure { error ->
                                isLoading = false
                                uiMessage = "Error: ${error.message}"
                            }
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }

    private fun ejecutarCasoUsoPartidos() {
        isLoading = true
        val fechaComienzo = fechaEnUnaSemanaTexto()

        partidoRepository.guardarPartidoConIdSecuencial(fechaComienzo) { escritura ->
            escritura
                .onSuccess { partidoGuardado ->
                    partidoRepository.obtenerTodosLosPartidos { lecturaCompleta ->
                        lecturaCompleta
                            .onSuccess {
                                partidoRepository.obtenerPartidosPorEstado("por_jugar") { lecturaFiltrada ->
                                    lecturaFiltrada
                                        .onSuccess {
                                            isLoading = false
                                            uiMessage = "Partido creado correctamente: ${partidoGuardado.id}"
                                        }
                                        .onFailure { error ->
                                            isLoading = false
                                            uiMessage = "Error: ${error.message}"
                                        }
                                }
                            }
                            .onFailure { error ->
                                isLoading = false
                                uiMessage = "Error: ${error.message}"
                            }
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }

    private fun ejecutarCasoUsoEquipos() {
        isLoading = true

        equipoRepository.guardarEquipoConIdSecuencial { escritura ->
            escritura
                .onSuccess { equipoGuardado ->
                    equipoRepository.obtenerTodosLosEquipos { lecturaCompleta ->
                        lecturaCompleta
                            .onSuccess {
                                equipoRepository.obtenerEquiposPorPais("España") { lecturaFiltrada ->
                                    lecturaFiltrada
                                        .onSuccess {
                                            isLoading = false
                                            uiMessage = "Equipo creado correctamente: ${equipoGuardado.id}"
                                        }
                                        .onFailure { error ->
                                            isLoading = false
                                            uiMessage = "Error: ${error.message}"
                                        }
                                }
                            }
                            .onFailure { error ->
                                isLoading = false
                                uiMessage = "Error: ${error.message}"
                            }
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }

    private fun ejecutarCasoUsoMiembros() {
        isLoading = true

        val ligaId = ultimaLigaId ?: "liga_amistosa1"
        val fechaEntrada = fechaActualIsoUtcTexto()

        usuarioRepository.obtenerTodosLosUsuarios { usuariosResult ->
            usuariosResult
                .onSuccess { usuarios ->
                    val usuarioSeleccionado = when {
                        usuarios.isEmpty() -> null
                        ultimoUsuarioId.isNullOrBlank() -> usuarios.first()
                        else -> usuarios.firstOrNull {
                            it.id == ultimoUsuarioId || it.usuarioId == ultimoUsuarioId
                        } ?: usuarios.first()
                    }

                    if (usuarioSeleccionado == null) {
                        isLoading = false
                        uiMessage = "No hay usuarios existentes para añadir como miembro"
                        return@onSuccess
                    }

                    val usuarioIdMiembro = usuarioSeleccionado.usuarioId.ifBlank { usuarioSeleccionado.id }

                    miembroRepository.guardarMiembro(
                        ligaId = ligaId,
                        usuarioId = usuarioIdMiembro,
                        nombre = usuarioSeleccionado.nombre,
                        fechaEntrada = fechaEntrada,
                        puntos = usuarioSeleccionado.puntos,
                        posicion = 1
                    ) { escritura ->
                        escritura
                            .onSuccess { miembroGuardado ->
                                miembroRepository.obtenerTodosLosMiembros(ligaId) { lecturaCompleta ->
                                    lecturaCompleta
                                        .onSuccess {
                                            miembroRepository.obtenerMiembrosPorPosicion(ligaId, 1) { lecturaFiltrada ->
                                                lecturaFiltrada
                                                    .onSuccess {
                                                        isLoading = false
                                                        uiMessage = "Miembro creado correctamente: ${miembroGuardado.usuarioId}"
                                                    }
                                                    .onFailure { error ->
                                                        isLoading = false
                                                        uiMessage = "Error: ${error.message}"
                                                    }
                                            }
                                        }
                                        .onFailure { error ->
                                            isLoading = false
                                            uiMessage = "Error: ${error.message}"
                                        }
                                }
                            }
                            .onFailure { error ->
                                isLoading = false
                                uiMessage = "Error: ${error.message}"
                            }
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }

    private fun ejecutarCasoUsoMensajesPantalla() {
        isLoading = true

        mensajePantallaRepository.guardarMensajeConIdSecuencial { escritura ->
            escritura
                .onSuccess { mensajeGuardado ->
                    mensajePantallaRepository.obtenerTodosLosMensajes { lecturaCompleta ->
                        lecturaCompleta
                            .onSuccess {
                                val mensajeId = mensajeGuardado.mensajeId.ifBlank { mensajeGuardado.id }
                                mensajePantallaRepository.obtenerMensajesPorId(mensajeId) { lecturaFiltrada ->
                                    lecturaFiltrada
                                        .onSuccess {
                                            isLoading = false
                                            uiMessage = "Mensaje creado correctamente: ${mensajeGuardado.mensajeId}"
                                        }
                                        .onFailure { error ->
                                            isLoading = false
                                            uiMessage = "Error: ${error.message}"
                                        }
                                }
                            }
                            .onFailure { error ->
                                isLoading = false
                                uiMessage = "Error: ${error.message}"
                            }
                    }
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }

    private fun ejecutarCasoUsoVaciarBaseDatos() {
        isLoading = true
        limpiezaFirestoreRepository.vaciarBaseDatos { resultado ->
            resultado
                .onSuccess {
                    ultimoUsuarioId = null
                    ultimaLigaId = null
                    isLoading = false
                    uiMessage = "Base de datos vaciada correctamente"
                }
                .onFailure { error ->
                    isLoading = false
                    uiMessage = "Error: ${error.message}"
                }
        }
    }
}