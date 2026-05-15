package com.example.kutira_kushala.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun observeRole(uid: String): Flow<String?> = callbackFlow {
        val reg = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.getString("role"))
            }
        awaitClose { reg.remove() }
    }

    suspend fun setRole(uid: String, role: String) {
        db.collection("users").document(uid)
            .set(mapOf("role" to role), SetOptions.merge())
            .await()
    }

    suspend fun getRole(uid: String): String? =
        db.collection("users").document(uid).get().await().getString("role")
}
