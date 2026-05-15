package com.example.kutira_kushala.data.repo

import com.example.kutira_kushala.data.DemoCatalog
import com.example.kutira_kushala.data.DirectoryFilter
import com.example.kutira_kushala.data.model.BusinessProfile
import com.example.kutira_kushala.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BusinessRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun observeDirectory(filter: DirectoryFilter): Flow<List<BusinessProfile>> = callbackFlow {
        var registration: ListenerRegistration? = null
        registration = db.collection("businesses").limit(100).addSnapshotListener { snap, error ->
            val remote = if (error != null || snap == null) {
                emptyList()
            } else {
                snap.documents
                    .map { BusinessProfile.fromSnapshot(it) }
                    .filter { passesDirectoryFilter(it, filter) }
                    .sortedBy { it.businessName.lowercase() }
            }
            trySend(mergeDemoProfiles(remote, filter))
        }
        awaitClose { registration?.remove() }
    }

    fun observeBusiness(businessId: String): Flow<BusinessProfile?> = callbackFlow {
        val fallback = DemoCatalog.profileById(businessId)
        val reg = db.collection("businesses").document(businessId)
            .addSnapshotListener { snap, _ ->
                when {
                    snap != null && snap.exists() -> trySend(BusinessProfile.fromSnapshot(snap))
                    fallback != null -> trySend(fallback)
                    else -> trySend(null)
                }
            }
        awaitClose { reg.remove() }
    }

    fun observeProducts(businessId: String): Flow<List<Product>> = callbackFlow {
        val reg = db.collection("businesses").document(businessId).collection("products")
            .orderBy("name")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map { Product.fromSnapshot(it, businessId) } ?: emptyList()
                val demo = DemoCatalog.demoProductsIfEmpty(businessId, list.size)
                trySend(if (demo.isNotEmpty()) demo else list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun ensureBusinessStub(ownerUid: String) {
        val ref = db.collection("businesses").document(ownerUid)
        val snap = ref.get().await()
        if (!snap.exists()) {
            ref.set(
                BusinessProfile(
                    id = ownerUid,
                    ownerUid = ownerUid,
                    businessName = "",
                ).toMap(),
                SetOptions.merge(),
            ).await()
        }
    }

    suspend fun saveBusiness(profile: BusinessProfile) {
        val ref = db.collection("businesses").document(profile.id)
        ref.set(profile.toMap().minus("categories"), SetOptions.merge()).await()
    }

    suspend fun updateCapacity(
        ownerUid: String,
        acceptingOrders: Boolean,
        dailyCapacityText: String,
        weeklyCapacityNote: String,
    ) {
        db.collection("businesses").document(ownerUid).update(
            mapOf(
                "acceptingOrders" to acceptingOrders,
                "dailyCapacityText" to dailyCapacityText,
                "weeklyCapacityNote" to weeklyCapacityNote,
            ),
        ).await()
    }

    suspend fun saveProduct(ownerUid: String, product: Product, existingId: String?): String {
        val col = db.collection("businesses").document(ownerUid).collection("products")
        val doc = if (existingId.isNullOrBlank()) col.document() else col.document(existingId)
        val toWrite = product.copy(id = doc.id, businessId = ownerUid)
        doc.set(toWrite.toMap()).await()
        recomputeCategories(ownerUid)
        return doc.id
    }

    suspend fun deleteProduct(ownerUid: String, productId: String) {
        db.collection("businesses").document(ownerUid).collection("products").document(productId)
            .delete().await()
        recomputeCategories(ownerUid)
    }

    suspend fun productCount(ownerUid: String): Int =
        db.collection("businesses").document(ownerUid).collection("products").get().await().size()

    suspend fun syncCategories(ownerUid: String) {
        recomputeCategories(ownerUid)
    }

    private suspend fun recomputeCategories(ownerUid: String) {
        val snap = db.collection("businesses").document(ownerUid).collection("products").get().await()
        val cats = snap.documents.mapNotNull { it.getString("category") }.distinct()
        db.collection("businesses").document(ownerUid).update("categories", cats).await()
    }

    companion object {
        const val MAX_PRODUCTS = 10
    }

    private fun mergeDemoProfiles(remote: List<BusinessProfile>, filter: DirectoryFilter): List<BusinessProfile> {
        val remoteIds = remote.map { it.id }.toSet()
        val extras = DemoCatalog.allProfiles().filter { demo ->
            demo.id !in remoteIds && DemoCatalog.passesDirectoryFilter(demo, filter)
        }
        return (remote + extras).sortedBy { it.businessName.lowercase() }
    }

    private fun passesDirectoryFilter(profile: BusinessProfile, filter: DirectoryFilter): Boolean {
        if (filter.onlyAcceptingOrders && !profile.acceptingOrders) return false
        val category = filter.category ?: return true
        return profile.categories.contains(category.firestoreValue)
    }
}
