package com.example.aplicacion.data.model

import com.google.firebase.firestore.DocumentId

data class Equipo(
    @DocumentId
    val id: String = "",
    val equipoId: String = "",
    val nombre: String = "",
    val pais: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "equipoId" to equipoId,
            "nombre" to nombre,
            "pais" to pais
        )
    }
}
