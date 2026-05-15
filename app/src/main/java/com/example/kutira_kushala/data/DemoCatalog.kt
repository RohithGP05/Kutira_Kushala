package com.example.kutira_kushala.data

import com.example.kutira_kushala.data.model.BusinessProfile
import com.example.kutira_kushala.data.model.Product
import com.example.kutira_kushala.data.model.ProductCategory

/**
 * Offline-friendly sample producers so buyers always see realistic listings while Firestore is empty
 * or unreachable. IDs are prefixed with `demo_` to avoid clashes with real Firebase document IDs.
 */
object DemoCatalog {
    private val profiles: List<BusinessProfile> = listOf(
        BusinessProfile(
            id = "demo_coorg_spices",
            ownerUid = "demo_coorg_spices",
            businessName = "Coorg Highland Spices",
            skillArea = "Organic pepper, cardamom, artisan pickles",
            locationText = "Madikeri town · wholesale crates",
            district = "Kodagu",
            state = "Karnataka",
            teamPhotoUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=800&q=80",
            acceptingOrders = true,
            dailyCapacityText = "Up to 200 kg graded pepper / day",
            weeklyCapacityNote = "Pickles bottled Tue–Thu; book 48h ahead.",
            contactPhone = "+91 9876500101",
            whatsappNumber = "919876500101",
            categories = listOf(ProductCategory.FOOD.firestoreValue),
        ),
        BusinessProfile(
            id = "demo_mysore_weavers",
            ownerUid = "demo_mysore_weavers",
            businessName = "Mysore Silk & Cotton Co-op",
            skillArea = "Handloom sarees, yardage, bulk uniforms",
            locationText = "Near Ashoka Road weaving cluster",
            district = "Mysuru",
            state = "Karnataka",
            teamPhotoUrl = "https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?w=800&q=80",
            acceptingOrders = true,
            dailyCapacityText = "450 metres woven fabric / day across looms",
            weeklyCapacityNote = "MOQ 30 metres per SKU for wholesale dye lots.",
            contactPhone = "+91 9876500202",
            whatsappNumber = "919876500202",
            categories = listOf(ProductCategory.TEXTILE.firestoreValue),
        ),
        BusinessProfile(
            id = "demo_channapatna_toys",
            ownerUid = "demo_channapatna_toys",
            businessName = "Channapatna Bright Crafts",
            skillArea = "Lac-turned wooden toys, décor export crates",
            locationText = "Toy town industrial zone",
            district = "Ramanagara",
            state = "Karnataka",
            teamPhotoUrl = "https://images.unsplash.com/photo-1515488047351-642db32670ca?w=800&q=80",
            acceptingOrders = false,
            dailyCapacityText = "Seasonal — reopening after lac curing week",
            weeklyCapacityNote = "Accepting enquiries only this fortnight.",
            contactPhone = "+91 9876500303",
            whatsappNumber = "",
            categories = listOf(ProductCategory.CRAFT.firestoreValue),
        ),
        BusinessProfile(
            id = "demo_udipi_supply",
            ownerUid = "demo_udipi_supply",
            businessName = "Udupi Kitchen Wholesale",
            skillArea = "Rice blends, spice powders, banana chips bulk",
            locationText = "Brahmavar packing unit",
            district = "Udupi",
            state = "Karnataka",
            teamPhotoUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&q=80",
            acceptingOrders = true,
            dailyCapacityText = "3 tonnes assorted staples / day",
            weeklyCapacityNote = "Cold-chain limited; chips pallet ships Mon/Wed/Fri.",
            contactPhone = "+91 9876500404",
            whatsappNumber = "919876500404",
            categories = listOf(ProductCategory.FOOD.firestoreValue, ProductCategory.OTHER.firestoreValue),
        ),
    )

    private val productsByBusiness: Map<String, List<Product>> = mapOf(
        "demo_coorg_spices" to listOf(
            Product(
                id = "p1",
                businessId = "demo_coorg_spices",
                name = "Malabar black pepper (Grade A)",
                description = "Sun-dried, density sorted; aroma-forward lots for retail packing.",
                wholesalePrice = 620.0,
                unit = "kg",
                category = ProductCategory.FOOD,
                imageUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400&q=80",
            ),
            Product(
                id = "p2",
                businessId = "demo_coorg_spices",
                name = "Green cardamom (8mm)",
                description = "Vacuum-ready export sacks; moisture logged per batch.",
                wholesalePrice = 1850.0,
                unit = "kg",
                category = ProductCategory.FOOD,
                imageUrl = "https://images.unsplash.com/photo-1599940824399-b87987ceb72a?w=400&q=80",
            ),
        ),
        "demo_mysore_weavers" to listOf(
            Product(
                id = "p1",
                businessId = "demo_mysore_weavers",
                name = "Mysore silk saree — jewel tones",
                description = "Traditional motifs; bulk for boutiques — assorted jewel palette.",
                wholesalePrice = 4200.0,
                unit = "piece",
                category = ProductCategory.TEXTILE,
                imageUrl = "https://images.unsplash.com/photo-1586790170083-2f9ceadc732d?w=400&q=80",
            ),
            Product(
                id = "p2",
                businessId = "demo_mysore_weavers",
                name = "Uniform khaki yardage",
                description = "Breathable cotton-poly blend; dyed to Pantone refs.",
                wholesalePrice = 185.0,
                unit = "metre",
                category = ProductCategory.TEXTILE,
                imageUrl = "https://images.unsplash.com/photo-1476179266333-d84ad845884f?w=400&q=80",
            ),
        ),
        "demo_channapatna_toys" to listOf(
            Product(
                id = "p1",
                businessId = "demo_channapatna_toys",
                name = "Classic rocking horse (medium)",
                description = "Non-toxic lac finish; EN-71 compliant export labeling available.",
                wholesalePrice = 890.0,
                unit = "piece",
                category = ProductCategory.CRAFT,
                imageUrl = "https://images.unsplash.com/photo-1515488047351-642db32670ca?w=400&q=80",
            ),
        ),
        "demo_udipi_supply" to listOf(
            Product(
                id = "p1",
                businessId = "demo_udipi_supply",
                name = "Sona masuri rice (26 kg sack)",
                description = "Polished premium; moisture-tested each intake.",
                wholesalePrice = 1420.0,
                unit = "sack",
                category = ProductCategory.FOOD,
                imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&q=80",
            ),
            Product(
                id = "p2",
                businessId = "demo_udipi_supply",
                name = "Banana chips — salted bulk",
                description = "Vacuum nitrogen flushed 5 kg cartons; shelf-stable 120 days.",
                wholesalePrice = 480.0,
                unit = "carton",
                category = ProductCategory.FOOD,
                imageUrl = "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=400&q=80",
            ),
        ),
    )

    fun allProfiles(): List<BusinessProfile> = profiles

    fun profileById(id: String): BusinessProfile? = profiles.firstOrNull { it.id == id }

    fun isDemoBusiness(id: String): Boolean = id.startsWith("demo_")

    fun demoProductsIfEmpty(businessId: String, remoteCount: Int): List<Product> {
        if (remoteCount > 0 || !isDemoBusiness(businessId)) return emptyList()
        return productsByBusiness[businessId].orEmpty()
    }

    fun passesDirectoryFilter(profile: BusinessProfile, filter: DirectoryFilter): Boolean {
        if (filter.onlyAcceptingOrders && !profile.acceptingOrders) return false
        val cat = filter.category ?: return true
        return profile.categories.contains(cat.firestoreValue)
    }
}
