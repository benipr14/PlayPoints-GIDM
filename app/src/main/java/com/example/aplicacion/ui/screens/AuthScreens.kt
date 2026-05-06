package com.example.aplicacion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aplicacion.ui.viewmodel.LoginViewModel
import com.example.aplicacion.data.model.Competicion
import com.example.aplicacion.data.model.Partido
import com.example.aplicacion.data.repository.CompeticionRepository
import com.example.aplicacion.data.repository.PartidoRepository
import com.example.aplicacion.ui.viewmodel.RegistrationViewModel
import com.example.aplicacion.data.model.Apuesta
import com.example.aplicacion.data.model.LigaAmistosa
import com.example.aplicacion.data.model.Miembro
import com.example.aplicacion.data.repository.ApuestaRepository
import com.example.aplicacion.data.repository.MensajePantallaRepository
import com.example.aplicacion.data.repository.LigaAmistosaRepository
import com.example.aplicacion.data.repository.MiembroRepository
import com.example.aplicacion.data.repository.UsuarioRepository
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import java.text.Normalizer

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegistered: () -> Unit,
    onGoToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe registration events
    LaunchedEffect(Unit) {
        viewModel.registeredEvent.collect {
            onRegistered()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMensaje ->
            snackbarHostState.showSnackbar(errorMensaje)
            // RegistrationViewModel no expone clearError(), solo mostramos el snackbar
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Registro", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = { Text("Email") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = uiState.nombre,
                    onValueChange = { viewModel.onNombreChanged(it) },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = { Text("Contraseña") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.padding(12.dp))
                Button(onClick = { viewModel.register() }, enabled = !uiState.isLoading) {
                    Text(text = if (uiState.isLoading) "Registrando..." else "Registrarse")
                }
                Spacer(modifier = Modifier.padding(10.dp))
                Text(text = "ó", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.padding(10.dp))
                Button(onClick = onGoToLogin, enabled = !uiState.isLoading) {
                    Text(text = "Iniciar sesión")
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loginSuccessEvent.collect {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMensaje ->
            snackbarHostState.showSnackbar(errorMensaje)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = uiState.nombre,
                    onValueChange = { viewModel.onNombreChanged(it) },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = { Text("Contraseña") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.padding(12.dp))
                Button(onClick = { viewModel.login() }, enabled = !uiState.isLoading) {
                    Text(text = if (uiState.isLoading) "Validando..." else "Entrar")
                }
                Spacer(modifier = Modifier.padding(4.dp))
                TextButton(onClick = onGoToRegister) {
                    Text(text = "¿No tienes cuenta? Regístrate")
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    onBuscadorClick: () -> Unit = {},
    onLigasAmistosasClick: () -> Unit = {},
    onHomeClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onMisApuestasClick: () -> Unit = {}
) {
    val secciones = listOf(
        "Buscador" to "🔎",
        "Ligas amistosas" to "🏆",
        "Mis apuestas" to "🎯",
        "Perfil" to "👤"
    )

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { index ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            onSelectedIndexChange(index)
                            if (index == 0) {
                                onBuscadorClick()
                            } else if (index == 1) {
                                onLigasAmistosasClick()
                            }
                        },
                        icon = {
                            Text(
                                text = secciones[index].second,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        label = {
                            Text(
                                text = secciones[index].first,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                ElevatedCard(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable(onClick = {
                            onSelectedIndexChange(0)
                            onHomeClick()
                        }),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🏠",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                repeat(2) { index ->
                    val itemIndex = index + 2
                    NavigationBarItem(
                        selected = selectedIndex == itemIndex,
                        onClick = {
                            onSelectedIndexChange(itemIndex)
                            if (itemIndex == 2) {
                                onMisApuestasClick()
                            } else if (itemIndex == 3) {
                                onPerfilClick()
                            }
                        },
                        icon = {
                            Text(
                                text = secciones[itemIndex].second,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        label = {
                            Text(
                                text = secciones[itemIndex].first,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToPerfil: () -> Unit,
    onNavigateToFutbol: () -> Unit,
    onNavigateToMisApuestas: () -> Unit = {}
    , onNavigateToBuscador: () -> Unit = {}
    , onNavigateToLigasAmistosas: () -> Unit = {}
) {
    var seccionSeleccionada by remember { mutableIntStateOf(1) }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedIndex = seccionSeleccionada,
                onSelectedIndexChange = { seccionSeleccionada = it },
                onBuscadorClick = onNavigateToBuscador,
                onLigasAmistosasClick = onNavigateToLigasAmistosas,
                onHomeClick = { },
                onPerfilClick = onNavigateToPerfil,
                onMisApuestasClick = onNavigateToMisApuestas
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Deportes Principales",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            val deportes = listOf(
                Triple("⚽ Fútbol", Color(0xFF1E88E5), Color(0xFF1565C0)),
                Triple("🏀 Baloncesto", Color(0xFFFF6F00), Color(0xFFE65100)),
                Triple("🎾 Tenis", Color(0xFF388E3C), Color(0xFF2E7D32)),
                Triple("🏈 American Football", Color(0xFF8B4513), Color(0xFF654321)),
                Triple("🏏 Cricket", Color(0xFFFF00FF), Color(0xFFCC00CC))
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(deportes.size) { index ->
                    val (nombre, colorPrimario) = deportes[index]
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable(onClick = {
                                if (index == 0) {
                                    onNavigateToFutbol()
                                }
                            }),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = colorPrimario
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = nombre.split(" ")[0],
                                style = MaterialTheme.typography.displaySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = nombre.split(" ", limit = 2)[1],
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LigasAmistosasScreen(
    ligaAmistosaRepository: LigaAmistosaRepository,
    usuarioActual: String,
    onNavigateToLigaMiembros: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToBuscador: () -> Unit = {},
    onNavigateToMisApuestas: () -> Unit = {}
) {
    var seccionSeleccionada by remember { mutableIntStateOf(1) }
    var ligas by remember { mutableStateOf<List<LigaAmistosa>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuarioActual) {
        isLoading = true
        ligaAmistosaRepository.obtenerLigasPorNombreMiembro(usuarioActual) { resultado ->
            resultado
                .onSuccess { lista ->
                    ligas = lista
                    errorMensaje = null
                    isLoading = false
                }
                .onFailure { error ->
                    errorMensaje = error.message ?: "No se pudieron cargar las ligas amistosas"
                    isLoading = false
                }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedIndex = seccionSeleccionada,
                onSelectedIndexChange = { seccionSeleccionada = it },
                onBuscadorClick = onNavigateToBuscador,
                onLigasAmistosasClick = { },
                onHomeClick = onNavigateToHome,
                onPerfilClick = onNavigateToPerfil,
                onMisApuestasClick = onNavigateToMisApuestas
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("<- Volver")
            }

            Text(
                text = "Ligas amistosas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            when {
                isLoading -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Buscando tus ligas...",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                errorMensaje != null -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMensaje ?: "Error desconocido",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                ligas.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🤝", style = MaterialTheme.typography.displaySmall)
                            Text(
                                text = "Aún no perteneces a ninguna liga amistosa",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Cuando te añadan como miembro con tu nombre, aparecerá aquí.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(ligas) { liga ->
                            val colorBase = if (liga.finalizada) Color(0xFF546E7A) else Color(0xFF2E7D32)
                            val ligaId = liga.id.ifBlank { liga.ligaId }
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (ligaId.isNotBlank()) {
                                            onNavigateToLigaMiembros(ligaId, liga.nombre)
                                        }
                                    },
                                colors = CardDefaults.elevatedCardColors(containerColor = colorBase),
                                shape = RoundedCornerShape(22.dp),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = liga.nombre,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Código: ${liga.codigo}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.92f)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = Color.White.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = if (liga.finalizada) "Finalizada" else "Activa",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Creada por ${liga.creadoPor} · ${liga.numMiembros} miembros",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.95f)
                                    )
                                    Text(
                                        text = "Toca para abrir la liga",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LigaMiembrosScreen(
    miembroRepository: MiembroRepository,
    ligaId: String,
    ligaNombre: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToMisApuestas: () -> Unit = {}
) {
    var seccionSeleccionada by remember { mutableIntStateOf(1) }
    var miembros by remember { mutableStateOf<List<Miembro>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var miembroSeleccionado by remember { mutableStateOf<Miembro?>(null) }

    LaunchedEffect(ligaId) {
        isLoading = true
        miembroRepository.obtenerTodosLosMiembros(ligaId) { resultado ->
            resultado
                .onSuccess { lista ->
                    miembros = lista.sortedWith(compareBy<Miembro> { it.posicion }.thenBy { it.nombre.lowercase() })
                    errorMensaje = null
                    isLoading = false
                }
                .onFailure { error ->
                    errorMensaje = error.message ?: "No se pudieron cargar los miembros"
                    isLoading = false
                }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedIndex = seccionSeleccionada,
                onSelectedIndexChange = { seccionSeleccionada = it },
                onHomeClick = onNavigateToHome,
                onPerfilClick = onNavigateToPerfil,
                onMisApuestasClick = onNavigateToMisApuestas
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text("<- Volver")
            }

            Text(
                text = if (ligaNombre.isBlank()) "Miembros de la liga" else ligaNombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            when {
                isLoading -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cargando miembros...",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                errorMensaje != null -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMensaje ?: "Error desconocido",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                miembros.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No hay miembros en esta liga",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(miembros) { miembro ->
                            val colorBase = when (miembro.posicion) {
                                1L -> Color(0xFF2E7D32)
                                2L -> Color(0xFF1E88E5)
                                3L -> Color(0xFFF9A825)
                                else -> Color(0xFF546E7A)
                            }

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { miembroSeleccionado = miembro },
                                colors = CardDefaults.elevatedCardColors(containerColor = colorBase),
                                shape = RoundedCornerShape(22.dp),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = miembro.nombre,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Posición ${miembro.posicion}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.92f)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = Color.White.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = "${miembro.puntos} pts",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Toca para ver la información del miembro",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (miembroSeleccionado != null) {
        MiembroDetalleDialog(
            miembro = miembroSeleccionado!!,
            onDismiss = { miembroSeleccionado = null }
        )
    }
}

@Composable
private fun MiembroDetalleDialog(
    miembro: Miembro,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Información del miembro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}

                DetalleRow(label = "Nombre:", valor = miembro.nombre)
                DetalleRow(label = "Usuario ID:", valor = miembro.usuarioId)
                DetalleRow(label = "Posición:", valor = miembro.posicion.toString())
                DetalleRow(label = "Puntos:", valor = miembro.puntos.toString())
                DetalleRow(label = "Fecha de entrada:", valor = miembro.fechaEntrada)

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun FootballCompetitionsScreen(
    competicionRepository: CompeticionRepository,
    usuarioRepository: UsuarioRepository,
    usuarioActual: String,
    onNavigateBack: () -> Unit,
    onNavigateToPartidos: (String, String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToMisApuestas: () -> Unit = {}
) {
    var seccionSeleccionada by remember { mutableIntStateOf(0) }
    var competiciones by remember { mutableStateOf<List<Competicion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        competicionRepository.obtenerCompeticionesPorDeporte("Fútbol") { resultado ->
            resultado
                .onSuccess { lista ->
                    competiciones = lista
                    isLoading = false
                    errorMensaje = null
                }
                .onFailure { error ->
                    errorMensaje = error.message ?: "No se pudieron cargar las competiciones"
                    isLoading = false
                }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedIndex = seccionSeleccionada,
                onSelectedIndexChange = { seccionSeleccionada = it },
                onHomeClick = onNavigateToHome,
                onPerfilClick = onNavigateToPerfil,
                onMisApuestasClick = onNavigateToMisApuestas
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text("<- Volver")
            }

            Text(
                text = "Competiciones de Fútbol",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            when {
                isLoading -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cargando competiciones...",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                errorMensaje != null -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMensaje ?: "Error desconocido",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                competiciones.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No hay competiciones de fútbol disponibles",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(competiciones) { competicion ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clickable(onClick = {
                                            // Navegar a la pantalla de partidos directamente. Si se requiere
                                            // sincronizar cooldown u otra lógica de usuario, usar
                                            // usuarioRepository.obtenerTodosLosUsuarios(...) o similar.
                                            onNavigateToPartidos(
                                                competicion.competicionId.ifBlank { competicion.id },
                                                competicion.nombre
                                            )
                                    }),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = Color(0xFF1E88E5)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = competicion.nombre,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    Text(
                                        text = competicion.temporada,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FootballMatchesScreen(
    partidoRepository: PartidoRepository,
    apuestaRepository: ApuestaRepository,
    mensajePantallaRepository: MensajePantallaRepository,
    usuarioRepository: UsuarioRepository,
    competicionId: String,
    competicionNombre: String,
    usuarioActual: String,
    fechaActualTexto: () -> String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToMisApuestas: () -> Unit = {}
) {
    var seccionSeleccionada by remember { mutableIntStateOf(0) }
    var partidos by remember { mutableStateOf<List<Partido>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(competicionId) {
        partidoRepository.obtenerPartidosPorCompeticionId(competicionId) { resultado ->
            resultado
                .onSuccess { lista ->
                    partidos = lista
                    isLoading = false
                    errorMensaje = null
                }
                .onFailure { error ->
                    errorMensaje = error.message ?: "No se pudieron cargar los partidos"
                    isLoading = false
                }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedIndex = seccionSeleccionada,
                onSelectedIndexChange = { seccionSeleccionada = it },
                onHomeClick = onNavigateToHome,
                onPerfilClick = onNavigateToPerfil,
                onMisApuestasClick = onNavigateToMisApuestas
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text("<- Volver")
            }

            Text(
                text = if (competicionNombre.isBlank()) "Partidos de la competición" else competicionNombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            when {
                isLoading -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cargando partidos...",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                errorMensaje != null -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMensaje ?: "Error desconocido",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                partidos.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No hay partidos disponibles para esta competición",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(partidos) { partido ->
                            PartidoCardConCuotas(
                                partido = partido,
                                apuestaRepository = apuestaRepository,
                                mensajePantallaRepository = mensajePantallaRepository,
                                usuarioRepository = usuarioRepository,
                                usuarioActual = usuarioActual,
                                fechaActualTexto = fechaActualTexto
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartidoCardConCuotas(
    partido: Partido,
    apuestaRepository: ApuestaRepository,
    mensajePantallaRepository: MensajePantallaRepository,
    usuarioRepository: UsuarioRepository,
    usuarioActual: String,
    fechaActualTexto: () -> String
) {
    var cuotaSeleccionada by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoApuesta by remember { mutableStateOf(false) }
    var cuotaSeleccionadaActual by remember { mutableStateOf<Triple<String, String, Double>?>(null) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E88E5)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = partido.local,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "vs",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Text(
                text = partido.visitante,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = partido.fechaComienzo,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Elige tu apuesta",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CuotaClickCard(
                    texto = partido.local,
                    cuota = partido.cuotaLocal,
                    seleccionado = cuotaSeleccionada == "local",
                    onClick = {
                        cuotaSeleccionada = "local"
                        cuotaSeleccionadaActual = Triple("local", partido.local, partido.cuotaLocal)
                        mostrarDialogoApuesta = true
                    },
                    modifier = Modifier.weight(1f)
                )
                CuotaClickCard(
                    texto = "Empate",
                    cuota = partido.cuotaEmpate,
                    seleccionado = cuotaSeleccionada == "empate",
                    onClick = {
                        cuotaSeleccionada = "empate"
                        cuotaSeleccionadaActual = Triple("empate", "Empate", partido.cuotaEmpate)
                        mostrarDialogoApuesta = true
                    },
                    modifier = Modifier.weight(1f)
                )
                CuotaClickCard(
                    texto = partido.visitante,
                    cuota = partido.cuotaVisitante,
                    seleccionado = cuotaSeleccionada == "visitante",
                    onClick = {
                        cuotaSeleccionada = "visitante"
                        cuotaSeleccionadaActual = Triple("visitante", partido.visitante, partido.cuotaVisitante)
                        mostrarDialogoApuesta = true
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Diálogo de apuesta
    if (mostrarDialogoApuesta && cuotaSeleccionadaActual != null) {
        val (seleccion, nombreSeleccion, cuota) = cuotaSeleccionadaActual!!
        DialogoApostador(
            partido = partido,
            seleccion = seleccion,
            nombreSeleccion = nombreSeleccion,
            cuota = cuota,
            apuestaRepository = apuestaRepository,
            mensajePantallaRepository = mensajePantallaRepository,
            usuarioRepository = usuarioRepository,
            usuarioActual = usuarioActual,
            fechaActualTexto = fechaActualTexto,
            onDismiss = {
                mostrarDialogoApuesta = false
                cuotaSeleccionada = null
                cuotaSeleccionadaActual = null
            },
            onApostado = {
                mostrarDialogoApuesta = false
                cuotaSeleccionada = null
                cuotaSeleccionadaActual = null
            }
        )
    }
}

@Composable
private fun CuotaClickCard(
    texto: String,
    cuota: Double,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .height(86.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (seleccionado) Color(0xFFFFC107) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = cuota.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SearchScreen(
    partidoRepository: PartidoRepository,
    competicionRepository: CompeticionRepository,
    onNavigateToPartidos: (String, String) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToLigasAmistosas: () -> Unit = {},
    onNavigateToMisApuestas: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var partidos by remember { mutableStateOf<List<Partido>>(emptyList()) }
    var competiciones by remember { mutableStateOf<List<Competicion>>(emptyList()) }

    LaunchedEffect(Unit) {
        isLoading = true
        partidoRepository.obtenerTodosLosPartidos { resP ->
            resP.onSuccess { partidos = it }
            // ignore failure here
        }
        competicionRepository.obtenerTodasLasCompeticiones { resC ->
            resC.onSuccess { competiciones = it }
            // ignore failure here
        }
        isLoading = false
    }

    val seccionSeleccionada = 0

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedIndex = seccionSeleccionada,
                onSelectedIndexChange = { },
                onBuscadorClick = { },
                onLigasAmistosasClick = onNavigateToLigasAmistosas,
                onHomeClick = onNavigateToHome,
                onPerfilClick = onNavigateToPerfil,
                onMisApuestasClick = onNavigateToMisApuestas
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextButton(onClick = onNavigateBack) { Text("<- Volver") }
            Text(text = "Buscador", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                // Normalizamos a NFC para que las combinaciones de teclas y caracteres
                // diacríticos (como 'u' + acento) se compongan correctamente en un único
                // carácter precompuesto (por ejemplo 'ú'). También forzamos teclado de
                // texto y una sola línea.
                onValueChange = { newValue ->
                    query = try {
                        Normalizer.normalize(newValue, Normalizer.Form.NFC)
                    } catch (e: Exception) {
                        newValue
                    }
                },
                label = { Text("Buscar partidos o competiciones") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Text("Cargando...", modifier = Modifier.padding(8.dp))
            } else {
                val q = query.trim()
                // Asociar competiciones por su id para poder filtrar partidos por deporte
                val competicionesMap = competiciones.associateBy { it.competicionId.ifBlank { it.id } }

                val partidosFiltrados = if (q.isBlank()) emptyList() else partidos.filter { partido ->
                    val coincideEquipos = partido.local.contains(q, ignoreCase = true) || partido.visitante.contains(q, ignoreCase = true)
                    val deporteDePartido = competicionesMap[partido.competicionId]?.deporte.orEmpty()
                    val coincideDeporte = deporteDePartido.contains(q, ignoreCase = true)
                    coincideEquipos || coincideDeporte
                }

                val competicionesFiltradas = if (q.isBlank()) emptyList() else competiciones.filter {
                    it.nombre.contains(q, ignoreCase = true) || it.pais.contains(q, ignoreCase = true) || it.deporte.contains(q, ignoreCase = true)
                }

                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (partidosFiltrados.isNotEmpty()) {
                        item { Text("Partidos", style = MaterialTheme.typography.titleMedium) }
                        items(partidosFiltrados) { partido ->
                            val competicionId = partido.competicionId
                            val competicionNombre = competicionesMap[competicionId]?.nombre.orEmpty()
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (competicionId.isNotBlank()) {
                                            onNavigateToPartidos(
                                                competicionId,
                                                competicionNombre.ifBlank { "Partidos" }
                                            )
                                        }
                                    }
                                    .padding(4.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E88E5))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(partido.local + " vs " + partido.visitante, color = Color.White)
                                    Text(partido.fechaComienzo, color = Color.White)
                                    if (competicionNombre.isNotBlank()) {
                                        Text(
                                            text = competicionNombre,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (competicionesFiltradas.isNotEmpty()) {
                        item { Text("Competiciones", style = MaterialTheme.typography.titleMedium) }
                        items(competicionesFiltradas) { competicion ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = {
                                        onNavigateToPartidos(competicion.competicionId.ifBlank { competicion.id }, competicion.nombre)
                                    })
                                    .padding(4.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(competicion.nombre, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(competicion.temporada, color = Color.White)
                                }
                            }
                        }
                    }

                    if (q.isNotBlank() && partidosFiltrados.isEmpty() && competicionesFiltradas.isEmpty()) {
                        item { Text("No se encontraron resultados para \"$q\"") }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    usuarioActual: String,
    onNavigateToHome: () -> Unit,
    onNavigateToBuscador: () -> Unit = {},
    onNavigateToLigasAmistosas: () -> Unit = {},
    onNavigateToMisApuestas: () -> Unit = {}
) {
    var seccionSeleccionada by remember { mutableIntStateOf(3) }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedIndex = seccionSeleccionada,
                onSelectedIndexChange = { seccionSeleccionada = it },
                onBuscadorClick = onNavigateToBuscador,
                onLigasAmistosasClick = onNavigateToLigasAmistosas,
                onHomeClick = onNavigateToHome,
                onPerfilClick = { },
                onMisApuestasClick = onNavigateToMisApuestas
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Perfil",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.padding(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mi Perfil",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = usuarioActual,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Text(
                text = "Más opciones del perfil próximamente...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun DialogoApostador(
    partido: Partido,
    seleccion: String,
    nombreSeleccion: String,
    cuota: Double,
    apuestaRepository: ApuestaRepository,
    mensajePantallaRepository: MensajePantallaRepository,
    usuarioRepository: UsuarioRepository,
    usuarioActual: String,
    fechaActualTexto: () -> String,
    onDismiss: () -> Unit,
    onApostado: () -> Unit
) {
    var cantidadApostada by remember { mutableStateOf("") }
    var errorValidacion by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var puntosUsuario by remember { mutableStateOf(0L) }
    var usuarioId by remember { mutableStateOf<String?>(null) }
    var cooldownActivo by remember { mutableStateOf(false) }
    var cooldownHasta by remember { mutableStateOf("") }
    var mensajePostApuesta by remember { mutableStateOf<String?>(null) }

    // Obtener información del usuario
    LaunchedEffect(usuarioActual) {
        usuarioRepository.obtenerTodosLosUsuarios { resultado ->
            resultado
                .onSuccess { usuarios ->
                    val usuario = usuarios.firstOrNull { it.nombre == usuarioActual }
                    if (usuario != null) {
                        usuarioId = usuario.id.ifBlank { usuario.usuarioId }
                        puntosUsuario = usuario.puntos
                        cooldownActivo = usuario.cooldownActivo
                        cooldownHasta = usuario.cooldownHasta
                    }
                }
                .onFailure {
                    // No hacemos nada especial aquí; valores por defecto se mantienen
                }
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Realizar Apuesta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Apuesta por: $nombreSeleccion",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Cuota: $cuota",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Puntos disponibles: $puntosUsuario",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (puntosUsuario > 0) Color.Green else Color.Red
                )

                OutlinedTextField(
                    value = cantidadApostada,
                    onValueChange = { 
                        cantidadApostada = it
                        errorValidacion = null
                    },
                    label = { Text("Cantidad de puntos") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorValidacion != null
                )

                if (errorValidacion != null) {
                    Text(
                        text = errorValidacion!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        enabled = !isProcessing
                    ) {
                        Text("No", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            // Validar apuesta
                            when {
                                cantidadApostada.isBlank() -> {
                                    errorValidacion = "Por favor ingresa una cantidad"
                                }
                                cantidadApostada.toLongOrNull() == null -> {
                                    errorValidacion = "La cantidad debe ser un número válido"
                                }
                                cantidadApostada.toLong() < 0 -> {
                                    errorValidacion = "No puedes apostar puntos negativos"
                                }
                                cantidadApostada.toLong() > puntosUsuario -> {
                                    errorValidacion = "No tienes suficientes puntos. Disponibles: $puntosUsuario"
                                }
                                cantidadApostada.toLong() == 0L -> {
                                    errorValidacion = "Debes apostar al menos 1 punto"
                                }
                                usuarioId == null -> {
                                    errorValidacion = "Error: No se pudo identificar el usuario"
                                }
                                cooldownActivo -> {
                                    errorValidacion = if (cooldownHasta.isBlank()) {
                                        "No puedes apostar ahora mismo porque tienes cooldown activo."
                                    } else {
                                        "No puedes apostar ahora mismo. Cooldown activo hasta: $cooldownHasta"
                                    }
                                }
                                else -> {
                                    // Proceder con la apuesta
                                    isProcessing = true
                                    val cantidad = cantidadApostada.toLong()
                                    val apuesta = Apuesta(
                                        partidoId = partido.id,
                                        competicionId = partido.competicionId,
                                        equipoLocal = partido.local,
                                        equipoVisitante = partido.visitante,
                                        seleccion = seleccion,
                                        cuota = cuota,
                                        cantidad = cantidad,
                                        estado = "pendiente",
                                        fechaApuesta = fechaActualTexto(),
                                        ligaID = ""
                                    )

                                    // Guardar apuesta (la implementación actual de
                                    // ApuestaRepository.guardarApuestaConIdSecuencial acepta
                                    // (usuarioId, fechaApuesta, onResult)). Ajustamos para usar
                                    // esa firma y luego actualizamos puntos y cooldown.
                                    apuestaRepository.guardarApuestaConIdSecuencial(
                                        usuarioId!!,
                                        fechaActualTexto()
                                    ) { resultadoApuesta ->
                                        resultadoApuesta
                                            .onSuccess { apuestaGuardada ->
                                                // Actualizar puntos del usuario
                                                val nuevosPuntos = puntosUsuario - cantidad
                                                usuarioRepository.actualizarPuntosUsuario(
                                                    usuarioId!!,
                                                    nuevosPuntos
                                                ) { resultadoActualizacion ->
                                                    isProcessing = false
                                                    resultadoActualizacion
                                                        .onSuccess {
                                                            // Activar cooldown por 1 hora calculando la fecha
                                                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                                            val cooldownFecha = java.util.Date(System.currentTimeMillis() + 60 * 60 * 1000)
                                                            val cooldownHastaStr = sdf.format(cooldownFecha)
                                                            usuarioRepository.actualizarCooldownUsuario(
                                                                usuarioId!!,
                                                                cooldownHastaStr
                                                            ) { resultadoCooldown ->
                                                                resultadoCooldown
                                                                    .onSuccess {
                                                                        cooldownActivo = true
                                                                        // Refrescar datos locales del usuario
                                                                        usuarioRepository.obtenerTodosLosUsuarios { usuarioResult ->
                                                                            usuarioResult
                                                                                .onSuccess { usuarios ->
                                                                                    val usuarioActualizado = usuarios.firstOrNull { it.nombre == usuarioActual }
                                                                                    cooldownHasta = usuarioActualizado?.cooldownHasta.orEmpty()
                                                                                    // Obtener mensaje aleatorio para mostrar
                                                                                    mensajePantallaRepository.obtenerMensajeAleatorio { mensajeResult ->
                                                                                        mensajePostApuesta = mensajeResult
                                                                                            .getOrNull()
                                                                                            ?.texto
                                                                                            ?.ifBlank { "Apuesta realizada correctamente." }
                                                                                            ?: "Apuesta realizada correctamente."
                                                                                    }
                                                                                }
                                                                                .onFailure {
                                                                                    // Ignorar fallo en refresco; mostramos igual el mensaje
                                                                                    mensajePantallaRepository.obtenerMensajeAleatorio { mensajeResult ->
                                                                                        mensajePostApuesta = mensajeResult
                                                                                            .getOrNull()
                                                                                            ?.texto
                                                                                            ?.ifBlank { "Apuesta realizada correctamente." }
                                                                                            ?: "Apuesta realizada correctamente."
                                                                                    }
                                                                                }
                                                                        }
                                                                    }
                                                                    .onFailure { error ->
                                                                        errorValidacion = "Apuesta guardada, pero no se pudo activar el cooldown: ${error.message}"
                                                                    }
                                                            }
                                                        }
                                                        .onFailure { error ->
                                                            errorValidacion = "Error al actualizar puntos: ${error.message}"
                                                        }
                                                }
                                            }
                                            .onFailure { error ->
                                                isProcessing = false
                                                errorValidacion = "Error al guardar apuesta: ${error.message}"
                                            }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    ) {
                        Text(if (isProcessing) "Procesando..." else "Sí")
                    }
                }
            }
        }
    }

    if (mensajePostApuesta != null) {
        MensajeApuestaDialog(
            mensaje = mensajePostApuesta.orEmpty(),
            onDismiss = {
                mensajePostApuesta = null
                onApostado()
            }
        )
    }
}

@Composable
private fun MensajeApuestaDialog(
    mensaje: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(onClick = onDismiss),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Mensaje",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .verticalScroll(rememberScrollState())
                )
                Text(
                    text = "Toca cualquier parte para cerrar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun MisApuestasScreen(
    usuarioActual: String,
    apuestaRepository: ApuestaRepository,
    usuarioRepository: UsuarioRepository,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToBuscador: () -> Unit = {},
    onNavigateToLigasAmistosas: () -> Unit = {},
    onNavigateToPerfil: () -> Unit
) {
    var seccionSeleccionada by remember { mutableIntStateOf(2) }
    var apuestas by remember { mutableStateOf<List<Apuesta>>(emptyList()) }
    var usuarioId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var apuestaSeleccionada by remember { mutableStateOf<Apuesta?>(null) }

    // Cargar apuestas del usuario
    LaunchedEffect(usuarioActual) {
        usuarioRepository.obtenerTodosLosUsuarios { resultado ->
            resultado.onSuccess { usuarios ->
                val usuario = usuarios.firstOrNull { it.nombre == usuarioActual }
                if (usuario != null) {
                    usuarioId = usuario.id.ifBlank { usuario.usuarioId }
                    usuarioId?.let { id ->
                        apuestaRepository.obtenerTodasLasApuestas(id) { resultadoApuestas ->
                            resultadoApuestas.onSuccess { listaApuestas ->
                                apuestas = listaApuestas
                                isLoading = false
                            }.onFailure {
                                isLoading = false
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedIndex = seccionSeleccionada,
                onSelectedIndexChange = { seccionSeleccionada = it },
                onBuscadorClick = onNavigateToBuscador,
                onLigasAmistosasClick = onNavigateToLigasAmistosas,
                onHomeClick = onNavigateToHome,
                onPerfilClick = onNavigateToPerfil,
                onMisApuestasClick = { }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("<- Volver")
            }

            Text(
                text = "Mis Apuestas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Cargando apuestas...")
                }
            } else if (apuestas.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No tienes apuestas aún",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(apuestas) { apuesta ->
                        ApuestaCard(
                            apuesta = apuesta,
                            onClicked = { apuestaSeleccionada = apuesta }
                        )
                    }
                }
            }
        }
    }

    // Diálogo con detalles de la apuesta
    if (apuestaSeleccionada != null) {
        DetallesApuestaDialog(
            apuesta = apuestaSeleccionada!!,
            onDismiss = { apuestaSeleccionada = null }
        )
    }
}

@Composable
private fun ApuestaCard(
    apuesta: Apuesta,
    onClicked: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClicked),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when (apuesta.estado) {
                "ganada" -> Color(0xFFC8E6C9)
                "perdida" -> Color(0xFFFFCDD2)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Equipos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = apuesta.equipoLocal,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "vs",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = apuesta.equipoVisitante,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            // Selección y cuota
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Selección: ${apuesta.seleccion}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Cuota: ${apuesta.cuota}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Cantidad y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Apuesta: ${apuesta.cantidad} pts",
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    color = when (apuesta.estado) {
                        "ganada" -> Color(0xFF4CAF50)
                        "perdida" -> Color(0xFFF44336)
                        else -> Color(0xFFFFC107)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = apuesta.estado.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp, 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetallesApuestaDialog(
    apuesta: Apuesta,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = "Detalles de la Apuesta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Separador
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}

                // Información del partido
                DetalleRow(label = "Equipo Local:", valor = apuesta.equipoLocal)
                DetalleRow(label = "Equipo Visitante:", valor = apuesta.equipoVisitante)

                // Información de la apuesta
                DetalleRow(label = "Selección:", valor = apuesta.seleccion)
                DetalleRow(label = "Cuota:", valor = apuesta.cuota.toString())
                DetalleRow(label = "Cantidad Apostada:", valor = "${apuesta.cantidad} puntos")

                // Información adicional
                DetalleRow(label = "Estado:", valor = apuesta.estado)
                DetalleRow(label = "Fecha de Apuesta:", valor = apuesta.fechaApuesta)
                DetalleRow(label = "ID Partida:", valor = apuesta.partidoId)
                DetalleRow(label = "ID Competición:", valor = apuesta.competicionId)

                // Botón cerrar
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun DetalleRow(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
