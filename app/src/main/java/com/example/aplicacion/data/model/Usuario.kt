package com.example.aplicacion.data.model

import com.google.firebase.firestore.DocumentId

data class Usuario(
    @DocumentId
    val id: String = "",
    val usuarioId: String = "",
    val nombre: String = "",
    val email: String = "",
    val role: String = "user",
    val fechaCreacion: String = "",
    val cooldownHasta: String = "",
    val cooldownActivo: Boolean = false,
    val puntos: Long = 0
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "usuarioId" to usuarioId,
            "nombre" to nombre,
            "email" to email,
            "role" to role,
            "fechaCreacion" to fechaCreacion,
            "cooldownHasta" to cooldownHasta,
            "cooldownActivo" to cooldownActivo,
            "puntos" to puntos
        )
    }
}
