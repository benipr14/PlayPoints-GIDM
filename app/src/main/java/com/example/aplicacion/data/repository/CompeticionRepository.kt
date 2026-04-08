package com.example.aplicacion.data.repository

import com.example.aplicacion.data.model.Competicion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class CompeticionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val competicionesCollection = firestore.collection("competiciones")

    fun guardarCompeticionConIdSecuencial(onResult: (Result<Competicion>) -> Unit) {
        competicionesCollection
            .get()
            .addOnSuccessListener { snapshot ->
                runCatching {
                    val idsExistentes = snapshot.documents.map { it.id }.toSet()

                    var numeroSiguiente = snapshot.size() + 1
                    var idSugerido = "competicion$numeroSiguiente"
                    while (idsExistentes.contains(idSugerido)) {
                        numeroSiguiente += 1
                        idSugerido = "competicion$numeroSiguiente"
                    }

                    val competicion = Competicion(
                        id = idSugerido,
                        competicionId = idSugerido,
                        nombre = "LaLiga EA Sports",
                        pais = "España",
                        deporte = "Fútbol",
                        temporada = "2024-2025"
                    )

                    competicionesCollection
                        .document(idSugerido)
                        .set(competicion.toFirestoreMap())
                        .addOnSuccessListener { onResult(Result.success(competicion)) }
                        .addOnFailureListener { error -> onResult(Result.failure(error)) }
                }.onFailure { error ->
                    onResult(Result.failure(error))
                }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerTodasLasCompeticiones(onResult: (Result<List<Competicion>>) -> Unit) {
        competicionesCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val competiciones = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toCompeticion() }.getOrNull()
                }
                onResult(Result.success(competiciones))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerCompeticionesPorPais(
        pais: String,
        onResult: (Result<List<Competicion>>) -> Unit
    ) {
        competicionesCollection
            .whereEqualTo("pais", pais)
            .get()
            .addOnSuccessListener { snapshot ->
                val competiciones = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toCompeticion() }.getOrNull()
                }
                onResult(Result.success(competiciones))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun DocumentSnapshot.toCompeticion(): Competicion {
        return Competicion(
            id = id,
            competicionId = leerTextoSeguro("competicionId").ifBlank { id },
            nombre = leerTextoSeguro("nombre"),
            pais = leerTextoSeguro("pais"),
            deporte = leerTextoSeguro("deporte"),
            temporada = leerTextoSeguro("temporada")
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
}
