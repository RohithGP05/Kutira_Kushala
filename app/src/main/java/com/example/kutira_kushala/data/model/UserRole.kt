package com.example.kutira_kushala.data.model

enum class UserRole(val firestoreValue: String) {
    PRODUCER("producer"),
    BUYER("buyer");

    companion object {
        fun fromFirestore(value: String?): UserRole? =
            entries.firstOrNull { it.firestoreValue == value }
    }
}
