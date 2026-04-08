package com.example.aplicacion.data.model

import com.google.firebase.firestore.DocumentId

data class MensajePantalla(
    @DocumentId
    val id: String = "",
    val mensajeId: String = "",
    val texto: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "mensajeId" to mensajeId,
            "texto" to texto
        )
    }
}
