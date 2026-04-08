package com.example.aplicacion.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore

class LimpiezaFirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun vaciarBaseDatos(onResult: (Result<Unit>) -> Unit) {
        eliminarSubcoleccionApuestasDeUsuarios { resultadoUsuarios ->
            resultadoUsuarios
                .onSuccess {
                    eliminarSubcoleccionMiembrosDeLigas { resultadoLigas ->
                        resultadoLigas
                            .onSuccess {
                                eliminarColeccionesRaiz(onResult)
                            }
                            .onFailure { error -> onResult(Result.failure(error)) }
                    }
                }
                .onFailure { error -> onResult(Result.failure(error)) }
        }
    }

    private fun eliminarSubcoleccionApuestasDeUsuarios(onResult: (Result<Unit>) -> Unit) {
        firestore.collection("usuarios")
            .get()
            .addOnSuccessListener { snapshot ->
                val tareas = snapshot.documents.map { userDoc ->
                    firestore.collection("usuarios").document(userDoc.id).collection("apuestas")
                        .get()
                        .continueWithTask { apuestaDocsTask ->
                            val apuestaDocs = apuestaDocsTask.result?.documents.orEmpty()
                            val borrados = apuestaDocs.map { it.reference.delete() }
                            Tasks.whenAllComplete(borrados)
                        }
                }

                Tasks.whenAllComplete(tareas)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { error -> onResult(Result.failure(error)) }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun eliminarSubcoleccionMiembrosDeLigas(onResult: (Result<Unit>) -> Unit) {
        firestore.collection("ligas_amistosas")
            .get()
            .addOnSuccessListener { snapshot ->
                val tareas = snapshot.documents.map { ligaDoc ->
                    firestore.collection("ligas_amistosas").document(ligaDoc.id).collection("miembros")
                        .get()
                        .continueWithTask { miembroDocsTask ->
                            val miembroDocs = miembroDocsTask.result?.documents.orEmpty()
                            val borrados = miembroDocs.map { it.reference.delete() }
                            Tasks.whenAllComplete(borrados)
                        }
                }

                Tasks.whenAllComplete(tareas)
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { error -> onResult(Result.failure(error)) }
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }

    private fun eliminarColeccionesRaiz(onResult: (Result<Unit>) -> Unit) {
        val colecciones = listOf(
            "mensajes_pantalla",
            "equipos",
            "partidos",
            "competiciones",
            "ligas_amistosas",
            "usuarios"
        )

        val tareas = colecciones.map { nombreColeccion ->
            firestore.collection(nombreColeccion)
                .get()
                .continueWithTask { docsTask ->
                    val docs = docsTask.result?.documents.orEmpty()
                    val borrados = docs.map { it.reference.delete() }
                    Tasks.whenAllComplete(borrados)
                }
        }

        Tasks.whenAllComplete(tareas)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }
}
