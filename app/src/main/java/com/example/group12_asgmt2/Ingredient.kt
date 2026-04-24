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

    @SerialName("purchase_date")
    val purchaseDate: String? = null,

    @SerialName("alert_enabled")
    val alertEnabled: Boolean? = true,

    @SerialName("quick_tag")
    val quickTag: String? = null,

    @SerialName("photo_url")
    val photoUrl: String? = null,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("created_at")
    val createdAt: String? = null
)

/** Payload used for INSERT and UPDATE */
@Serializable
data class IngredientPayload(
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val price: Double,

    @SerialName("expiry_date")
    val expiryDate: String,

    @SerialName("purchase_date")
    val purchaseDate: String? = null,

    @SerialName("alert_enabled")
    val alertEnabled: Boolean? = true,

    @SerialName("quick_tag")
    val quickTag: String? = null,

    @SerialName("photo_url")
    val photoUrl: String? = null
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

enum class FoodUnit(val symbol: String) {
    GRAM("g"),
    KILOGRAM("kg"),
    MILLIGRAM("mg"),
    OUNCE("oz"),
    POUND("lb"),
    MILLILITRE("mL"),
    LITRE("L"),
    CUP("cup"),
    TABLESPOON("tbsp"),
    TEASPOON("tsp"),
    FLUID_OUNCE("fl oz"),
    PIECE("pcs"),
    DOZEN("dozen"),
    PACK("pack"),
    CAN("can"),
    SLICE("slice"),
    BUNCH("bunch");

    companion object {
        fun symbols(): List<String> = entries.map { it.symbol }
        fun fromSymbol(symbol: String): FoodUnit? = entries.find { it.symbol == symbol }
    }
}