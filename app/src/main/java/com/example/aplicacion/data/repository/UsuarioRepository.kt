package com.example.aplicacion.data.repository

import com.example.aplicacion.data.model.Usuario
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

class UsuarioRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usuariosCollection = firestore.collection("usuarios")

    fun guardarUsuarioConIdSecuencial(
        usuario: Usuario,
        onResult: (Result<String>) -> Unit
    ) {
        usuariosCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val idsExistentes = snapshot.documents.map { it.id }.toSet()
                var numeroSiguiente = snapshot.size() + 1
                var idSugerido = "usuario$numeroSiguiente"

                while (idsExistentes.contains(idSugerido)) {
                    numeroSiguiente += 1
                    idSugerido = "usuario$numeroSiguiente"
                }

                guardarUsuario(idSugerido, usuario) { resultadoEscritura ->
                    resultadoEscritura
                        .onSuccess { onResult(Result.success(idSugerido)) }
                        .onFailure { error -> onResult(Result.failure(error)) }
                }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun guardarUsuario(documentId: String, usuario: Usuario, onResult: (Result<Unit>) -> Unit) {
        val usuarioConId = usuario.copy(
            id = documentId,
            usuarioId = documentId
        )

        usuariosCollection
            .document(documentId)
            .set(usuarioConId.toFirestoreMap())
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun actualizarCooldownUsuario(
        documentId: String,
        cooldownHasta: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        usuariosCollection
            .document(documentId)
            .update(
                mapOf(
                    "cooldownActivo" to true,
                    "cooldownHasta" to cooldownHasta
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun actualizarPuntosUsuario(
        documentId: String,
        nuevosPuntos: Long,
        onResult: (Result<Unit>) -> Unit
    ) {
        usuariosCollection
            .document(documentId)
            .update(
                mapOf(
                    "puntos" to nuevosPuntos
                )
            )
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerTodosLosUsuarios(onResult: (Result<List<Usuario>>) -> Unit) {
        usuariosCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val usuarios = snapshot.documents.mapNotNull { document -> document.toUsuario() }
                onResult(Result.success(usuarios))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun buscarUsuarioPorEmail(email: String, onResult: (Result<List<Usuario>>) -> Unit) {
        usuariosCollection
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                val usuarios = snapshot.documents.mapNotNull { document -> document.toUsuario() }
                onResult(Result.success(usuarios))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun buscarUsuarioPorNombreYPassword(
        nombre: String,
        password: String,
        onResult: (Result<List<Usuario>>) -> Unit
    ) {
        usuariosCollection
            .whereEqualTo("nombre", nombre)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { snapshot ->
                val usuarios = snapshot.documents.mapNotNull { document -> document.toUsuario() }
                onResult(Result.success(usuarios))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerUsuariosConCooldownActivo(
        cooldownActivo: Boolean,
        onResult: (Result<List<Usuario>>) -> Unit
    ) {
        usuariosCollection
            .whereEqualTo("cooldownActivo", cooldownActivo)
            .get()
            .addOnSuccessListener { snapshot ->
                val usuarios = snapshot.documents.mapNotNull { document -> document.toUsuario() }
                onResult(Result.success(usuarios))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun DocumentSnapshot.toUsuario(): Usuario {
        return Usuario(
            id = id,
            usuarioId = (getString("usuarioId") ?: "").ifBlank { id },
            nombre = getString("nombre") ?: "",
            email = getString("email") ?: "",
            role = getString("role") ?: "user",
            password = getString("password") ?: "",
            fechaCreacion = leerTextoOFecha("fechaCreacion"),
            cooldownHasta = leerTextoOFecha("cooldownHasta"),
            cooldownActivo = getBoolean("cooldownActivo") ?: false,
            puntos = leerPuntos("puntos")
        )
    }


    private fun DocumentSnapshot.leerTextoOFecha(campo: String): String {
        val valor = get(campo)
        return when (valor) {
            is String -> valor
            is Timestamp -> valor.toDate().toString()
            else -> ""
        }
    }

    private fun DocumentSnapshot.leerPuntos(campo: String): Long {
        val valor = get(campo)
        return when (valor) {
            is Long -> valor
            is Int -> valor.toLong()
            is Double -> valor.toLong()
            is Number -> valor.toLong()
            else -> 0L
        }
    }
}
