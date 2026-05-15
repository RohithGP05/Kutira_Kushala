package com.example.kutira_kushala.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class Product(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val description: String = "",
    val wholesalePrice: Double = 0.0,
    val unit: String = "",
    val category: ProductCategory = ProductCategory.OTHER,
    val quantityText: String = "",
    val weeklyCapacityText: String = "",
    val imageUrl: String = "",
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "description" to description,
        "wholesalePrice" to wholesalePrice,
        "unit" to unit,
        "category" to category.firestoreValue,
        "quantityText" to quantityText,
        "weeklyCapacityText" to weeklyCapacityText,
        "imageUrl" to imageUrl,
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot, businessId: String): Product =
            Product(
                id = doc.id,
                businessId = businessId,
                name = doc.getString("name").orEmpty(),
                description = doc.getString("description").orEmpty(),
                wholesalePrice = doc.getDouble("wholesalePrice") ?: 0.0,
                unit = doc.getString("unit").orEmpty(),
                category = ProductCategory.fromFirestore(doc.getString("category")),
                quantityText = doc.getString("quantityText").orEmpty(),
                weeklyCapacityText = doc.getString("weeklyCapacityText").orEmpty(),
                imageUrl = doc.getString("imageUrl").orEmpty(),
            )
    }
}
