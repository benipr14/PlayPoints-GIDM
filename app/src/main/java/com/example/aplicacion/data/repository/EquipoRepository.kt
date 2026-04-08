package com.example.aplicacion.data.repository

import com.example.aplicacion.data.model.Equipo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class EquipoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val equiposCollection = firestore.collection("equipos")

    fun guardarEquipoConIdSecuencial(onResult: (Result<Equipo>) -> Unit) {
        equiposCollection
            .get()
            .addOnSuccessListener { snapshot ->
                runCatching {
                    val idsExistentes = snapshot.documents.map { it.id }.toSet()

                    var numeroSiguiente = snapshot.size() + 1
                    var idSugerido = "equipo$numeroSiguiente"
                    while (idsExistentes.contains(idSugerido)) {
                        numeroSiguiente += 1
                        idSugerido = "equipo$numeroSiguiente"
                    }

                    val equipo = Equipo(
                        id = idSugerido,
                        equipoId = idSugerido,
                        nombre = "Real Madrid Club de Fútbol",
                        pais = "España"
                    )

                    equiposCollection
                        .document(idSugerido)
                        .set(equipo.toFirestoreMap())
                        .addOnSuccessListener { onResult(Result.success(equipo)) }
                        .addOnFailureListener { error -> onResult(Result.failure(error)) }
                }.onFailure { error ->
                    onResult(Result.failure(error))
                }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerTodosLosEquipos(onResult: (Result<List<Equipo>>) -> Unit) {
        equiposCollection
            .get()
            .addOnSuccessListener { snapshot ->
                val equipos = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toEquipo() }.getOrNull()
                }
                onResult(Result.success(equipos))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    fun obtenerEquiposPorPais(
        pais: String,
        onResult: (Result<List<Equipo>>) -> Unit
    ) {
        equiposCollection
            .whereEqualTo("pais", pais)
            .get()
            .addOnSuccessListener { snapshot ->
                val equipos = snapshot.documents.mapNotNull { document ->
                    runCatching { document.toEquipo() }.getOrNull()
                }
                onResult(Result.success(equipos))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun DocumentSnapshot.toEquipo(): Equipo {
        return Equipo(
            id = id,
            equipoId = leerTextoSeguro("equipoId"),
            nombre = leerTextoSeguro("nombre"),
            pais = leerTextoSeguro("pais")
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
}
