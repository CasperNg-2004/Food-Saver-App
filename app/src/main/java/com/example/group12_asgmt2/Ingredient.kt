package com.example.group12_asgmt2

data class Ingredient(
    val id: String? = null,

    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val price: Double,

    val expiryDate: String,

    val purchaseDate: String? = null,

    val alertEnabled: Boolean? = true,

    val quickTag: String? = null,

    val photoUrl: String? = null,

    val createdAt: String? = null,

    val status: String? = "ACTIVE",

    val resolvedDate: String? = null
)

/** Payload used for INSERT and UPDATE */
data class IngredientPayload(
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val price: Double,

    val expiryDate: String,

    val purchaseDate: String? = null,

    val alertEnabled: Boolean? = true,

    val quickTag: String? = null,

    val photoUrl: String? = null,

    val status: String? = "ACTIVE",

    val resolvedDate: String? = null
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
