package com.example.aplicacion.data.model

import com.google.firebase.firestore.DocumentId

data class Competicion(
    @DocumentId
    val id: String = "",
    val competicionId: String = "",
    val nombre: String = "",
    val pais: String = "",
    val deporte: String = "",
    val temporada: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "competicionId" to competicionId,
            "nombre" to nombre,
            "pais" to pais,
            "deporte" to deporte,
            "temporada" to temporada
        )
    }
}
