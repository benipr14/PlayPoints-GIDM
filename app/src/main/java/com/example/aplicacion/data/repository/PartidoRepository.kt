package com.example.aplicacion.data.repository

import com.example.aplicacion.data.model.Partido
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class PartidoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val partidosCollection = firestore.collection("partidos")

    fun guardarPartidoConIdSecuencial(
        fechaComienzo: String,
        onResult: (Result<Partido>) -> Unit
    ) {
        partidosCollection
            .get()
            .addOnSuccessListener { snapshot ->
                runCatching {
                    val idsExistentes = snapshot.documents.map { it.id }.toSet()

                    var numeroSiguiente = snapshot.size() + 1
                    var idSugerido = "partido$numeroSiguiente"
                    while (idsExistentes.contains(idSugerido)) {
                        numeroSiguiente += 1
                        idSugerido = "partido$numeroSiguiente"
                    }

                    val partido = Partido(
                        id = idSugerido,
                        partidoId = idSugerido,
                        competicionId = "competicion1",
                        localId = "equipo1",
                        visitanteID = "equipo2",
                        local = "Real Madrid Club de Fútbol",
                        visitante = "Fútbol Club Barcelona",
                        fechaComienzo = fechaComienzo,
                        estado = "por_jugar",
                        cuotaLocal = 1.85,
                        cuotaEmpate = 3.4,
                        cuotaVisitante = 2.25
                    )

                    partidosCollection
                        .document(idSugerido)
                        .set(partido.toFirestoreMap())
                        .addOnSuccessListener { onResult(Result.success(partido)) }
                        .addOnFailureListener { error -> onResult(Result.failure(error)) }
                }.onFailure { error ->
                    onResult(Result.failure(error))
                }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerTodosLosPartidos(onResult: (Result<List<Partido>>) -> Unit) {
        partidosCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val partidos = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toPartido() }.getOrNull()
                }
                onResult(Result.success(partidos))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerPartidosPorEstado(
        estado: String,
        onResult: (Result<List<Partido>>) -> Unit
    ) {
        partidosCollection
            .whereEqualTo("estado", estado)
            .get()
            .addOnSuccessListener { snapshot ->
                val partidos = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toPartido() }.getOrNull()
                }
                onResult(Result.success(partidos))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerPartidosPorCompeticionId(
        competicionId: String,
        onResult: (Result<List<Partido>>) -> Unit
    ) {
        partidosCollection
            .whereEqualTo("competicionId", competicionId)
            .get()
            .addOnSuccessListener { snapshot ->
                val partidos = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toPartido() }.getOrNull()
                }
                onResult(Result.success(partidos))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun DocumentSnapshot.toPartido(): Partido {
        return Partido(
            id = id,
            partidoId = leerTextoSeguro("partidoId").ifBlank { id },
            competicionId = leerTextoSeguro("competicionId"),
            localId = leerTextoSeguro("localId"),
            visitanteID = leerTextoSeguro("visitanteID"),
            local = leerTextoSeguro("local"),
            visitante = leerTextoSeguro("visitante"),
            fechaComienzo = leerTextoSeguro("fechaComienzo"),
            estado = leerTextoSeguro("estado"),
            cuotaLocal = leerDoubleSeguro("cuotaLocal"),
            cuotaEmpate = leerDoubleSeguro("cuotaEmpate"),
            cuotaVisitante = leerDoubleSeguro("cuotaVisitante")
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

    private fun DocumentSnapshot.leerDoubleSeguro(campo: String): Double {
        val valor = get(campo)
        return when (valor) {
            is Double -> valor
            is Float -> valor.toDouble()
            is Long -> valor.toDouble()
            is Int -> valor.toDouble()
            is Number -> valor.toDouble()
            is String -> valor.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
}
