package com.example.aplicacion.data.model

import com.google.firebase.firestore.DocumentId

data class LigaAmistosa(
    @DocumentId
    val id: String = "",
    val ligaId: String = "",
    val nombre: String = "",
    val creadoPor: String = "",
    val creadoEn: String = "",
    val codigo: String = "",
    val finalizada: Boolean = false,
    val numMiembros: Long = 0
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "ligaId" to ligaId,
            "nombre" to nombre,
            "creadoPor" to creadoPor,
            "creadoEn" to creadoEn,
            "codigo" to codigo,
            "finalizada" to finalizada,
            "numMiembros" to numMiembros
        )
    }
}
