package com.example.kutira_kushala.data.model

enum class ProductCategory(val label: String, val firestoreValue: String) {
    FOOD("Food", "FOOD"),
    CRAFT("Craft", "CRAFT"),
    TEXTILE("Textile", "TEXTILE"),
    OTHER("Other", "OTHER");

    companion object {
        fun fromFirestore(value: String?): ProductCategory =
            entries.firstOrNull { it.firestoreValue == value } ?: OTHER
    }
}
