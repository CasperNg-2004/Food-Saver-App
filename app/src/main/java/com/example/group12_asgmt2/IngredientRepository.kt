package com.example.group12_asgmt2

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

object IngredientRepository {

    private val supabase = SupabaseManager.client
    private const val TABLE = "ingredients"

    /** Fetch all ingredients sorted by expiry date ascending. */
    suspend fun getAll(): List<Ingredient> {
        return supabase.from(TABLE)
            .select {
                filter { eq("status", "ACTIVE") }
                order(column = "expiry_date", order = Order.ASCENDING)
            }
            .decodeList<Ingredient>()
    }

    /** Fetch ingredients filtered by category, sorted by expiry date ascending. */
    suspend fun getByCategory(category: String): List<Ingredient> {
        return supabase.from(TABLE)
            .select {
                filter { 
                    eq("category", category) 
                    eq("status", "ACTIVE")
                }
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

    @kotlinx.serialization.Serializable
    private data class StatusUpdate(
        val status: String,
        @kotlinx.serialization.SerialName("resolved_date") val resolvedDate: String
    )

    /** Mark an ingredient as consumed. */
    suspend fun markAsConsumed(id: String, date: String) {
        supabase.from(TABLE).update(StatusUpdate("CONSUMED", date)) {
            filter { eq("id", id) }
        }
    }

    /** Mark an ingredient as wasted. */
    suspend fun markAsWasted(id: String, date: String) {
        supabase.from(TABLE).update(StatusUpdate("WASTED", date)) {
            filter { eq("id", id) }
        }
    }

    /** Fetch historical data (CONSUMED or WASTED items). */
    suspend fun getHistoricalData(): List<Ingredient> {
        return supabase.from(TABLE)
            .select {
                filter { neq("status", "ACTIVE") }
            }
            .decodeList<Ingredient>()
    }
}
