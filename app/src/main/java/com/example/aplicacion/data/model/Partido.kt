package com.example.aplicacion.data.model

import com.google.firebase.firestore.DocumentId

data class Partido(
    @DocumentId
    val id: String = "",
    val partidoId: String = "",
    val competicionId: String = "",
    val localId: String = "",
    val visitanteID: String = "",
    val local: String = "",
    val visitante: String = "",
    val fechaComienzo: String = "",
    val estado: String = "",
    val cuotaLocal: Double = 0.0,
    val cuotaEmpate: Double = 0.0,
    val cuotaVisitante: Double = 0.0
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "partidoId" to partidoId,
            "competicionId" to competicionId,
            "localId" to localId,
            "visitanteID" to visitanteID,
            "local" to local,
            "visitante" to visitante,
            "fechaComienzo" to fechaComienzo,
            "estado" to estado,
            "cuotaLocal" to cuotaLocal,
            "cuotaEmpate" to cuotaEmpate,
            "cuotaVisitante" to cuotaVisitante
        )
    }
}
