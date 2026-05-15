package com.example.kutira_kushala.data.repo

import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

class StorageRepository(
    private val storage: FirebaseStorage = defaultStorage(),
) {
    suspend fun uploadBytes(path: String, bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        val ref = storage.reference.child(path)
        val meta = StorageMetadata.Builder().setContentType("image/jpeg").build()
        try {
            ref.putBytes(bytes, meta).await()
            return ref.downloadUrl.await().toString()
        } catch (e: StorageException) {
            throw IllegalStateException(storageMessage(e), e)
        }
    }

    private fun storageMessage(e: StorageException): String =
        when (e.errorCode) {
            StorageException.ERROR_OBJECT_NOT_FOUND ->
                "Firebase Storage could not find the bucket/object. In Firebase Console, open Storage, click Get started, and publish rules for businesses/{uid}/ images."
            StorageException.ERROR_BUCKET_NOT_FOUND ->
                "Storage bucket not found. Confirm the bucket in google-services.json exists in Firebase Storage."
            StorageException.ERROR_PROJECT_NOT_FOUND ->
                "Firebase project not found. Check google-services.json package / project setup."
            StorageException.ERROR_QUOTA_EXCEEDED ->
                "Storage quota exceeded in Firebase console."
            StorageException.ERROR_NOT_AUTHENTICATED ->
                "Sign in required for uploads."
            StorageException.ERROR_NOT_AUTHORIZED ->
                "Permission denied by Storage rules. Allow authenticated writes under businesses/{uid}/…"
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
                "Upload timed out — check network and try again."
            else -> e.message?.takeIf { it.isNotBlank() }
                ?: "Upload failed (Firebase Storage code ${e.errorCode})."
        }

    companion object {
        private fun defaultStorage(): FirebaseStorage {
            val bucket = FirebaseApp.getInstance().options.storageBucket
            return if (bucket.isNullOrBlank()) {
                FirebaseStorage.getInstance()
            } else {
                FirebaseStorage.getInstance("gs://$bucket")
            }
        }
    }
}
