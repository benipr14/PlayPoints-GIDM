package com.example.aplicacion.data.repository

import com.example.aplicacion.data.model.Miembro
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MiembroRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun miembrosSubcollection(ligaId: String) =
        firestore.collection("ligas_amistosas").document(ligaId).collection("miembros")

    fun guardarMiembro(
        ligaId: String,
        usuarioId: String,
        nombre: String,
        fechaEntrada: String,
        puntos: Long,
        posicion: Long,
        onResult: (Result<Miembro>) -> Unit
    ) {
        val miembro = Miembro(
            id = usuarioId,
            usuarioId = usuarioId,
            nombre = nombre,
            fechaEntrada = fechaEntrada,
            puntos = puntos,
            posicion = posicion
        )

        miembrosSubcollection(ligaId)
            .document(usuarioId)
            .set(miembro.toFirestoreMap())
            .addOnSuccessListener { onResult(Result.success(miembro)) }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerTodosLosMiembros(
        ligaId: String,
        onResult: (Result<List<Miembro>>) -> Unit
    ) {
        miembrosSubcollection(ligaId)
            .get()
            .addOnSuccessListener { snapshot ->
                val miembros = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toMiembro() }.getOrNull()
                }
                onResult(Result.success(miembros))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerMiembrosPorPosicion(
        ligaId: String,
        posicion: Long,
        onResult: (Result<List<Miembro>>) -> Unit
    ) {
        miembrosSubcollection(ligaId)
            .whereEqualTo("posicion", posicion)
            .get()
            .addOnSuccessListener { snapshot ->
                val miembros = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toMiembro() }.getOrNull()
                }
                onResult(Result.success(miembros))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun DocumentSnapshot.toMiembro(): Miembro {
        return Miembro(
            id = id,
            usuarioId = leerTextoSeguro("usuarioId").ifBlank { id },
            nombre = leerTextoSeguro("nombre"),
            fechaEntrada = leerTextoSeguro("fechaEntrada"),
            puntos = leerLongSeguro("puntos"),
            posicion = leerLongSeguro("posicion")
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
}
