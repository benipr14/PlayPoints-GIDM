package com.example.aplicacion.data.repository

import com.example.aplicacion.data.model.LigaAmistosa
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class LigaAmistosaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val ligasCollection = firestore.collection("ligas_amistosas")
    private val miembroRepository = MiembroRepository(firestore)

    fun guardarLigaConIdSecuencial(
        nombre: String,
        creadoPor: String,
        creadoEn: String,
        numMiembros: Long,
        onResult: (Result<LigaAmistosa>) -> Unit
    ) {
        ligasCollection
            .get()
            .addOnSuccessListener { snapshot ->
                runCatching {
                    val idsExistentes = snapshot.documents.map { it.id }.toSet()
                    val codigosExistentes = snapshot.documents
                        .mapNotNull { (it.get("codigo") as? String) }
                        .toSet()

                    var numeroSiguiente = snapshot.size() + 1
                    var idSugerido = "liga_amistosa$numeroSiguiente"
                    while (idsExistentes.contains(idSugerido)) {
                        numeroSiguiente += 1
                        idSugerido = "liga_amistosa$numeroSiguiente"
                    }

                    val codigoUnico = generarCodigoUnico(codigosExistentes)
                    val liga = LigaAmistosa(
                        id = idSugerido,
                        ligaId = idSugerido,
                        nombre = nombre,
                        creadoPor = creadoPor,
                        creadoEn = creadoEn,
                        codigo = codigoUnico,
                        finalizada = false,
                        numMiembros = numMiembros
                    )

                    ligasCollection
                        .document(idSugerido)
                        .set(liga.toFirestoreMap())
                        .addOnSuccessListener { onResult(Result.success(liga)) }
                        .addOnFailureListener { error -> onResult(Result.failure(error)) }
                }.onFailure { error ->
                    onResult(Result.failure(error))
                }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerTodasLasLigas(onResult: (Result<List<LigaAmistosa>>) -> Unit) {
        ligasCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val ligas = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toLigaAmistosa() }.getOrNull()
                }
                onResult(Result.success(ligas))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerLigasNoFinalizadas(onResult: (Result<List<LigaAmistosa>>) -> Unit) {
        ligasCollection
            .whereEqualTo("finalizada", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val ligas = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toLigaAmistosa() }.getOrNull()
                }
                onResult(Result.success(ligas))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerLigasPorNombreMiembro(
        nombreMiembro: String,
        onResult: (Result<List<LigaAmistosa>>) -> Unit
    ) {
        val nombreBuscado = nombreMiembro.trim()
        if (nombreBuscado.isBlank()) {
            onResult(Result.success(emptyList()))
            return
        }

        obtenerTodasLasLigas { resultadoLigas ->
            resultadoLigas
                .onSuccess { ligas ->
                    if (ligas.isEmpty()) {
                        onResult(Result.success(emptyList()))
                        return@onSuccess
                    }

                    val ligasCoincidentes = mutableListOf<LigaAmistosa>()
                    val pendientes = AtomicInteger(ligas.size)
                    val resultadoEmitido = AtomicBoolean(false)

                    ligas.forEach { liga ->
                        val ligaId = liga.id.ifBlank { liga.ligaId }
                        miembroRepository.obtenerTodosLosMiembros(ligaId) { miembrosResult ->
                            miembrosResult.onSuccess { miembros ->
                                val pertenece = miembros.any { miembro ->
                                    miembro.nombre.trim().equals(nombreBuscado, ignoreCase = true)
                                }
                                if (pertenece) {
                                    ligasCoincidentes.add(liga)
                                }
                            }

                            if (pendientes.decrementAndGet() == 0 && resultadoEmitido.compareAndSet(false, true)) {
                                onResult(
                                    Result.success(
                                        ligasCoincidentes.sortedBy { it.nombre.lowercase() }
                                    )
                                )
                            }
                        }
                    }
                }
                .onFailure { error -> onResult(Result.failure(error)) }
        }
    }

    private fun generarCodigoUnico(codigosExistentes: Set<String>): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var codigo = ""
        do {
            codigo = buildString {
                repeat(6) {
                    append(caracteres[Random.nextInt(caracteres.length)])
                }
            }
        } while (codigosExistentes.contains(codigo))
        return codigo
    }

    private fun DocumentSnapshot.toLigaAmistosa(): LigaAmistosa {
        return LigaAmistosa(
            id = id,
            ligaId = leerTextoSeguro("ligaId").ifBlank { id },
            nombre = leerTextoSeguro("nombre"),
            creadoPor = leerTextoSeguro("creadoPor"),
            creadoEn = leerTextoSeguro("creadoEn"),
            codigo = leerTextoSeguro("codigo"),
            finalizada = leerBooleanSeguro("finalizada"),
            numMiembros = leerLongSeguro("numMiembros")
        )
    }

    private fun DocumentSnapshot.leerTextoSeguro(campo: String): String {
        val valor = get(campo)
        return when (valor) {
            null -> ""
            is String -> valor
            else -> valor.toString()
        }
    }

    private fun DocumentSnapshot.leerBooleanSeguro(campo: String): Boolean {
        val valor = get(campo)
        return when (valor) {
            is Boolean -> valor
            is String -> valor.equals("true", ignoreCase = true)
            is Number -> valor.toInt() != 0
            else -> false
        }
    }

    private fun DocumentSnapshot.leerLongSeguro(campo: String): Long {
        val valor = get(campo)
        return when (valor) {
            is Long -> valor
            is Int -> valor.toLong()
            is Double -> valor.toLong()
            is Number -> valor.toLong()
            is String -> valor.toLongOrNull() ?: 0L
            else -> 0L
        }
    }
}
