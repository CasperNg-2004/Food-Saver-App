package com.example.group12_asgmt2

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Ingredient(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val id: String? = null,

    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val price: Double,

    @SerialName("expiry_date")
    val expiryDate: String,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("created_at")
    val createdAt: String? = null
)

/** Lightweight payload used for INSERT and UPDATE (excludes server-generated fields). */
@Serializable
data class IngredientPayload(
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val price: Double,
    @SerialName("expiry_date")
    val expiryDate: String
)

enum class Category(val displayName: String) {
    PROTEIN("Protein"),
    VITAMIN("Vitamin"),
    DAIRY("Dairy"),
    GRAIN("Grain"),
    VEGETABLE("Vegetable"),
    FRUIT("Fruit"),
    SPICE("Spice"),
    OTHER("Other");

    companion object {
        fun displayNames() = entries.map { it.displayName }
    }
}

/** Standardised units — shared across ingredients & recipe pages for quantity matching. */
enum class FoodUnit(val symbol: String) {
    // Weight
    GRAM("g"),
    KILOGRAM("kg"),
    MILLIGRAM("mg"),
    OUNCE("oz"),
    POUND("lb"),

    // Volume
    MILLILITRE("mL"),
    LITRE("L"),
    CUP("cup"),
    TABLESPOON("tbsp"),
    TEASPOON("tsp"),
    FLUID_OUNCE("fl oz"),

    // Count / Pack
    PIECE("pcs"),
    DOZEN("dozen"),
    PACK("pack"),
    CAN("can"),
    SLICE("slice"),
    BUNCH("bunch");

    companion object {
        fun symbols(): List<String> = entries.map { it.symbol }

        /** Find enum by symbol — useful in recipe matching logic. */
        fun fromSymbol(symbol: String): FoodUnit? = entries.find { it.symbol == symbol }
    }
}
