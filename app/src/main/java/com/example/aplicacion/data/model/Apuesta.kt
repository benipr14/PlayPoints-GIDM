package com.example.aplicacion.data.model

import com.google.firebase.firestore.DocumentId

data class Apuesta(
    @DocumentId
    val id: String = "",
    val partidoId: String = "",
    val competicionId: String = "",
    val equipoLocal: String = "",
    val equipoVisitante: String = "",
    val seleccion: String = "",
    val cuota: Double = 0.0,
    val cantidad: Long = 0,
    val estado: String = "pendiente",
    val fechaApuesta: String = "",
    val ligaID: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "partidoId" to partidoId,
            "competicionId" to competicionId,
            "equipoLocal" to equipoLocal,
            "equipoVisitante" to equipoVisitante,
            "seleccion" to seleccion,
            "cuota" to cuota,
            "cantidad" to cantidad,
            "estado" to estado,
            "fechaApuesta" to fechaApuesta,
            "ligaID" to ligaID
        )
    }
}
