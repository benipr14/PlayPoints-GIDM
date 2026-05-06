package com.example.aplicacion.data.repository

import com.example.aplicacion.data.model.Apuesta
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ApuestaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun apuestasSubcollection(usuarioId: String) =
        firestore.collection("usuarios").document(usuarioId).collection("apuestas")

    fun guardarApuestaConIdSecuencial(
        usuarioId: String,
        fechaApuesta: String,
        onResult: (Result<Apuesta>) -> Unit
    ) {
        val apuestasRef = apuestasSubcollection(usuarioId)

        apuestasRef
            .get()
            .addOnSuccessListener { snapshot ->
                runCatching {
                    val idsExistentes = snapshot.documents.map { it.id }.toSet()

                    var numeroSiguiente = snapshot.size() + 1
                    var idSugerido = "apuesta$numeroSiguiente"
                    while (idsExistentes.contains(idSugerido)) {
                        numeroSiguiente += 1
                        idSugerido = "apuesta$numeroSiguiente"
                    }

                    val apuesta = Apuesta(
                        id = idSugerido,
                        partidoId = "partido$numeroSiguiente",
                        competicionId = "competicion1",
                        equipoLocal = "Real Madrid",
                        equipoVisitante = "Barcelona",
                        seleccion = "local",
                        cuota = 1.85,
                        cantidad = 10,
                        estado = "pendiente",
                        fechaApuesta = fechaApuesta,
                        ligaID = "liga_amistosa1"
                    )

                    apuestasRef
                        .document(idSugerido)
                        .set(apuesta.toFirestoreMap())
                        .addOnSuccessListener { onResult(Result.success(apuesta)) }
                        .addOnFailureListener { error -> onResult(Result.failure(error)) }
                }.onFailure { error ->
                    onResult(Result.failure(error))
                }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerTodasLasApuestas(
        usuarioId: String,
        onResult: (Result<List<Apuesta>>) -> Unit
    ) {
        apuestasSubcollection(usuarioId)
            .get()
            .addOnSuccessListener { snapshot ->
                val apuestas = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toApuesta() }.getOrNull()
                }
                onResult(Result.success(apuestas))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerApuestasPendientes(
        usuarioId: String,
        onResult: (Result<List<Apuesta>>) -> Unit
    ) {
        apuestasSubcollection(usuarioId)
            .whereEqualTo("estado", "pendiente")
            .get()
            .addOnSuccessListener { snapshot ->
                val apuestas = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toApuesta() }.getOrNull()
                }
                onResult(Result.success(apuestas))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun DocumentSnapshot.toApuesta(): Apuesta {
        return Apuesta(
            id = id,
            partidoId = leerTextoSeguro("partidoId"),
            competicionId = leerTextoSeguro("competicionId"),
            equipoLocal = leerTextoSeguro("equipoLocal"),
            equipoVisitante = leerTextoSeguro("equipoVisitante"),
            seleccion = leerTextoSeguro("seleccion"),
            cuota = leerDoubleSeguro("cuota"),
            cantidad = leerLongSeguro("cantidad"),
            estado = leerTextoSeguro("estado"),
            fechaApuesta = leerTextoSeguro("fechaApuesta"),
            ligaID = leerTextoSeguro("ligaID")
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
