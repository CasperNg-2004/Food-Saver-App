package com.group12.ingredientmanager.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

object IngredientRepository {

    private val supabase = SupabaseManager.client
    private const val TABLE = "ingredients"

    /** Fetch all ingredients sorted by expiry date ascending. */
    suspend fun getAll(): List<Ingredient> {
        return supabase.from(TABLE)
            .select {
                order(column = "expiry_date", order = Order.ASCENDING)
            }
            .decodeList<Ingredient>()
    }

    /** Fetch ingredients filtered by category, sorted by expiry date ascending. */
    suspend fun getByCategory(category: String): List<Ingredient> {
        return supabase.from(TABLE)
            .select {
                filter { eq("category", category) }
                order(column = "expiry_date", order = Order.ASCENDING)
            }
            .decodeList<Ingredient>()
    }

    /** Insert a new ingredient. */
    suspend fun insert(payload: IngredientPayload) {
        supabase.from(TABLE).insert(payload)
    }

    /** Update an existing ingredient by id. */
    suspend fun update(id: String, payload: IngredientPayload) {
        supabase.from(TABLE).update(payload) {
            filter { eq("id", id) }
        }
    }

    /** Delete an ingredient by id. */
    suspend fun delete(id: String) {
        supabase.from(TABLE).delete {
            filter { eq("id", id) }
        }
    }
}
