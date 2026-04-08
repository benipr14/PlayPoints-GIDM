package com.example.aplicacion.data.repository

import com.example.aplicacion.data.model.MensajePantalla
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MensajePantallaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val mensajesCollection = firestore.collection("mensajes_pantalla")

    fun guardarMensajeConIdSecuencial(onResult: (Result<MensajePantalla>) -> Unit) {
        mensajesCollection
            .get()
            .addOnSuccessListener { snapshot ->
                runCatching {
                    val idsExistentes = snapshot.documents.map { it.id }.toSet()

                    var numeroSiguiente = snapshot.size() + 1
                    var idSugerido = "mensaje$numeroSiguiente"
                    while (idsExistentes.contains(idSugerido)) {
                        numeroSiguiente += 1
                        idSugerido = "mensaje$numeroSiguiente"
                    }

                    val mensaje = MensajePantalla(
                        id = idSugerido,
                        mensajeId = idSugerido,
                        texto = "Los problemas relacionados con el juego pueden afectar gravemente a la salud mental y a la economía personal."
                    )

                    mensajesCollection
                        .document(idSugerido)
                        .set(mensaje.toFirestoreMap())
                        .addOnSuccessListener { onResult(Result.success(mensaje)) }
                        .addOnFailureListener { error -> onResult(Result.failure(error)) }
                }.onFailure { error ->
                    onResult(Result.failure(error))
                }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerTodosLosMensajes(onResult: (Result<List<MensajePantalla>>) -> Unit) {
        mensajesCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val mensajes = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toMensajePantalla() }.getOrNull()
                }
                onResult(Result.success(mensajes))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerMensajesPorId(
        mensajeId: String,
        onResult: (Result<List<MensajePantalla>>) -> Unit
    ) {
        mensajesCollection
            .whereEqualTo("mensajeId", mensajeId)
            .get()
            .addOnSuccessListener { snapshot ->
                val mensajes = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toMensajePantalla() }.getOrNull()
                }
                onResult(Result.success(mensajes))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun DocumentSnapshot.toMensajePantalla(): MensajePantalla {
        return MensajePantalla(
            id = id,
            mensajeId = leerTextoSeguro("mensajeId").ifBlank { id },
            texto = leerTextoSeguro("texto")
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
