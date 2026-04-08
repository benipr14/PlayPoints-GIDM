package com.example.aplicacion.data.model

import com.google.firebase.firestore.DocumentId

data class Miembro(
    @DocumentId
    val id: String = "",
    val usuarioId: String = "",
    val nombre: String = "",
    val fechaEntrada: String = "",
    val puntos: Long = 0,
    val posicion: Long = 0
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "usuarioId" to usuarioId,
            "nombre" to nombre,
            "fechaEntrada" to fechaEntrada,
            "puntos" to puntos,
            "posicion" to posicion
        )
    }
}
