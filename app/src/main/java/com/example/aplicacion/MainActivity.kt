package com.example.aplicacion

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.aplicacion.data.repository.ApuestaRepository
import com.example.aplicacion.data.repository.CompeticionRepository
import com.example.aplicacion.data.model.Usuario
import com.example.aplicacion.data.model.Apuesta
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
    private var usuarioActual by mutableStateOf("usuario1")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Prueba_firebaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                            // Use Navigation Compose to handle navigation between screens
                                    val navController = androidx.navigation.compose.rememberNavController()
                                    // Create ViewModels using ViewModelProvider so they survive config changes
                                    val registrationViewModel = remember {
                                        androidx.lifecycle.ViewModelProvider(
                                            this@MainActivity,
                                            com.example.aplicacion.ui.viewmodel.RegistrationViewModelFactory(usuarioRepository, ::fechaActualTexto)
                                        ).get(com.example.aplicacion.ui.viewmodel.RegistrationViewModel::class.java)
                                    }

                                    val loginViewModel = remember {
                                        androidx.lifecycle.ViewModelProvider(
                                            this@MainActivity,
                                            com.example.aplicacion.ui.viewmodel.LoginViewModelFactory(usuarioRepository)
                                        ).get(com.example.aplicacion.ui.viewmodel.LoginViewModel::class.java)
                                    }

                                    androidx.navigation.compose.NavHost(navController = navController, startDestination = "register") {
                                        composable("register") {
                                            com.example.aplicacion.ui.screens.RegistrationScreen(
                                                viewModel = registrationViewModel,
                                                onRegistered = {
                                                    usuarioActual = registrationViewModel.uiState.value.nombre
                                                    navController.navigate("home")
                                                },
                                                onGoToLogin = { navController.navigate("login") }
                                            )
                                        }
                                        composable("login") {
                                            com.example.aplicacion.ui.screens.LoginScreen(
                                                viewModel = loginViewModel,
                                                onLoginSuccess = {
                                                    usuarioActual = loginViewModel.uiState.value.nombre
                                                    navController.navigate("home")
                                                },
                                                onGoToRegister = { navController.navigate("register") }
                                            )
                                        }
                                        composable("home") {
                                            com.example.aplicacion.ui.screens.HomeScreen(
                                                onNavigateToPerfil = { navController.navigate("perfil") },
                                                onNavigateToFutbol = { navController.navigate("futbol_competiciones") },
                                                onNavigateToMisApuestas = { navController.navigate("mis_apuestas") },
                                                onNavigateToBuscador = { navController.navigate("buscador") },
                                                onNavigateToLigasAmistosas = { navController.navigate("ligas_amistosas") }
                                            )
                                        }
                                        composable("ligas_amistosas") {
                                            com.example.aplicacion.ui.screens.LigasAmistosasScreen(
                                                ligaAmistosaRepository = ligaAmistosaRepository,
                                                usuarioActual = usuarioActual,
                                                onNavigateToLigaMiembros = { ligaId, ligaNombre ->
                                                    navController.navigate(
                                                        "liga_miembros/${Uri.encode(ligaId)}/${Uri.encode(ligaNombre)}"
                                                    )
                                                },
                                                onNavigateBack = { navController.popBackStack() },
                                                onNavigateToHome = { navController.navigate("home") },
                                                onNavigateToPerfil = { navController.navigate("perfil") },
                                                onNavigateToBuscador = { navController.navigate("buscador") },
                                                onNavigateToMisApuestas = { navController.navigate("mis_apuestas") }
                                            )
                                        }
                                        composable(
                                            route = "liga_miembros/{ligaId}/{ligaNombre}",
                                            arguments = listOf(
                                                navArgument("ligaId") { type = NavType.StringType },
                                                navArgument("ligaNombre") { type = NavType.StringType }
                                            )
                                        ) { backStackEntry ->
                                            val ligaId = backStackEntry.arguments?.getString("ligaId").orEmpty()
                                            val ligaNombre = backStackEntry.arguments?.getString("ligaNombre").orEmpty()
                                            com.example.aplicacion.ui.screens.LigaMiembrosScreen(
                                                miembroRepository = miembroRepository,
                                                ligaId = ligaId,
                                                ligaNombre = ligaNombre,
                                                onNavigateBack = { navController.popBackStack() },
                                                onNavigateToHome = { navController.navigate("home") },
                                                onNavigateToPerfil = { navController.navigate("perfil") },
                                                onNavigateToMisApuestas = { navController.navigate("mis_apuestas") }
                                            )
                                        }
                                        composable("futbol_competiciones") {
                                            com.example.aplicacion.ui.screens.FootballCompetitionsScreen(
                                                competicionRepository = competicionRepository,
                                                usuarioRepository = usuarioRepository,
                                                usuarioActual = usuarioActual,
                                                onNavigateBack = { navController.popBackStack() },
                                                onNavigateToPartidos = { competicionId, competicionNombre ->
                                                    navController.navigate(
                                                        "partidos_competicion/${Uri.encode(competicionId)}/${Uri.encode(competicionNombre)}"
                                                    )
                                                },
                                                onNavigateToHome = { navController.navigate("home") },
                                                onNavigateToPerfil = { navController.navigate("perfil") },
                                                onNavigateToMisApuestas = { navController.navigate("mis_apuestas") }
                                            )
                                        }
                                        composable(
                                            route = "partidos_competicion/{competicionId}/{competicionNombre}",
                                            arguments = listOf(
                                                navArgument("competicionId") { type = NavType.StringType },
                                                navArgument("competicionNombre") { type = NavType.StringType }
                                            )
                                        ) { backStackEntry ->
                                             val competicionId = backStackEntry.arguments?.getString("competicionId").orEmpty()
                                             val competicionNombre = backStackEntry.arguments?.getString("competicionNombre").orEmpty()
                                             com.example.aplicacion.ui.screens.FootballMatchesScreen(
                                                 partidoRepository = partidoRepository,
                                                 apuestaRepository = apuestaRepository,
                                                 mensajePantallaRepository = mensajePantallaRepository,
                                                 usuarioRepository = usuarioRepository,
                                                 competicionId = competicionId,
                                                 competicionNombre = competicionNombre,
                                                 usuarioActual = usuarioActual,
                                                 fechaActualTexto = ::fechaActualTexto,
                                                 onNavigateBack = { navController.popBackStack() },
                                                 onNavigateToHome = { navController.navigate("home") },
                                                 onNavigateToPerfil = { navController.navigate("perfil") },
                                                 onNavigateToMisApuestas = { navController.navigate("mis_apuestas") }
                                             )
                                        }
                                        composable("mis_apuestas") {
                                            com.example.aplicacion.ui.screens.MisApuestasScreen(
                                                usuarioActual = usuarioActual,
                                                apuestaRepository = apuestaRepository,
                                                usuarioRepository = usuarioRepository,
                                                onNavigateBack = { navController.popBackStack() },
                                                onNavigateToHome = { navController.navigate("home") },
                                                onNavigateToBuscador = { navController.navigate("buscador") },
                                                onNavigateToLigasAmistosas = { navController.navigate("ligas_amistosas") },
                                                onNavigateToPerfil = { navController.navigate("perfil") }
                                            )
                                        }
                                        composable("perfil") {
                                            com.example.aplicacion.ui.screens.ProfileScreen(
                                                usuarioActual = usuarioActual,
                                                onNavigateToHome = { navController.navigate("home") },
                                                onNavigateToBuscador = { navController.navigate("buscador") },
                                                onNavigateToLigasAmistosas = { navController.navigate("ligas_amistosas") },
                                                onNavigateToMisApuestas = { navController.navigate("mis_apuestas") }
                                            )
                                        }
                                        composable("buscador") {
                                            com.example.aplicacion.ui.screens.SearchScreen(
                                                partidoRepository = partidoRepository,
                                                competicionRepository = competicionRepository,
                                                onNavigateToPartidos = { competicionId, competicionNombre ->
                                                    navController.navigate("partidos_competicion/${Uri.encode(competicionId)}/${Uri.encode(competicionNombre)}")
                                                },
                                                onNavigateBack = { navController.popBackStack() },
                                                onNavigateToHome = { navController.navigate("home") },
                                                onNavigateToPerfil = { navController.navigate("perfil") },
                                                onNavigateToLigasAmistosas = { navController.navigate("ligas_amistosas") },
                                                onNavigateToMisApuestas = { navController.navigate("mis_apuestas") }
                                            )
                                        }
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

        val apuestaEjemplo = Apuesta(
            partidoId = "partido1",
            competicionId = "competicion1",
            equipoLocal = "Real Madrid",
            equipoVisitante = "Barcelona",
            seleccion = "local",
            cuota = 1.85,
            cantidad = 10,
            estado = "pendiente",
            fechaApuesta = fechaApuesta,
            ligaID = ""
        )

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

    // Screen composables have been moved to `ui.screens.AuthScreens` and navigation is handled with Navigation Compose.
}