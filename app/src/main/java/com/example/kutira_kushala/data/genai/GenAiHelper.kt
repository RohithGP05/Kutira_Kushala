package com.example.kutira_kushala.data.genai

import com.example.kutira_kushala.data.model.ProductCategory

/**
 * PRD GenAI layer — template-based MVP (swap for Gemini / remote API when keys are configured).
 */
object GenAiHelper {
    fun suggestProductDescription(
        businessSkill: String,
        productName: String,
        category: ProductCategory,
    ): String {
        val skill = businessSkill.trim().ifBlank { "handcrafted goods" }
        val name = productName.trim().ifBlank { "this product" }
        return buildString {
            append("$name is made with care as part of our $skill work. ")
            append(
                when (category) {
                    ProductCategory.FOOD -> "Prepared in small batches for consistent quality and freshness. "
                    ProductCategory.CRAFT -> "Traditional craft techniques give each piece a unique finish. "
                    ProductCategory.TEXTILE -> "Durable stitching and neat finishing suitable for regular use. "
                    ProductCategory.OTHER -> "Suitable for bulk buyers looking for reliable local supply. "
                },
            )
            append("Ideal for wholesale buyers seeking transparent pricing and steady capacity.")
        }
    }

    fun suggestBusinessTagline(skillArea: String, district: String): String {
        val s = skillArea.trim().ifBlank { "cottage industry" }
        val d = district.trim().ifBlank { "our region" }
        return "Trusted $s from $d — ready for bulk orders with clear pricing."
    }
}
