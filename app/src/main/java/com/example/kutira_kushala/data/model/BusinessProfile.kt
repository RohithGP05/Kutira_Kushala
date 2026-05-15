package com.example.kutira_kushala.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class BusinessProfile(
    val id: String = "",
    val ownerUid: String = "",
    val businessName: String = "",
    val skillArea: String = "",
    val locationText: String = "",
    val district: String = "",
    val state: String = "",
    val teamPhotoUrl: String = "",
    val acceptingOrders: Boolean = false,
    val dailyCapacityText: String = "",
    val weeklyCapacityNote: String = "",
    val contactPhone: String = "",
    val whatsappNumber: String = "",
    val categories: List<String> = emptyList(),
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "ownerUid" to ownerUid,
        "businessName" to businessName,
        "skillArea" to skillArea,
        "locationText" to locationText,
        "district" to district,
        "state" to state,
        "teamPhotoUrl" to teamPhotoUrl,
        "acceptingOrders" to acceptingOrders,
        "dailyCapacityText" to dailyCapacityText,
        "weeklyCapacityNote" to weeklyCapacityNote,
        "contactPhone" to contactPhone,
        "whatsappNumber" to whatsappNumber,
        "categories" to categories,
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): BusinessProfile {
            val cats = (doc.get("categories") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            return BusinessProfile(
                id = doc.id,
                ownerUid = doc.getString("ownerUid") ?: doc.id,
                businessName = doc.getString("businessName").orEmpty(),
                skillArea = doc.getString("skillArea").orEmpty(),
                locationText = doc.getString("locationText").orEmpty(),
                district = doc.getString("district").orEmpty(),
                state = doc.getString("state").orEmpty(),
                teamPhotoUrl = doc.getString("teamPhotoUrl").orEmpty(),
                acceptingOrders = doc.getBoolean("acceptingOrders") ?: false,
                dailyCapacityText = doc.getString("dailyCapacityText").orEmpty(),
                weeklyCapacityNote = doc.getString("weeklyCapacityNote").orEmpty(),
                contactPhone = doc.getString("contactPhone").orEmpty(),
                whatsappNumber = doc.getString("whatsappNumber").orEmpty(),
                categories = cats,
            )
        }
    }
}
