package com.example.group12_asgmt2

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

object IngredientRepository {
    private const val STORAGE_KEY = "ingredients"
    private const val ACTIVE = "ACTIVE"
    private val gson = Gson()
    private val lock = Mutex()

    suspend fun getAll(): List<Ingredient> = withContext(Dispatchers.IO) {
        lock.withLock {
            readAll().filter { it.status == ACTIVE }.sortedBy { it.expiryDate }
        }
    }

    suspend fun getByCategory(category: String): List<Ingredient> = withContext(Dispatchers.IO) {
        lock.withLock {
            readAll()
                .filter { it.status == ACTIVE && it.category == category }
                .sortedBy { it.expiryDate }
        }
    }

    suspend fun insert(payload: IngredientPayload) = withContext(Dispatchers.IO) {
        lock.withLock {
            val ingredients = readAll().toMutableList()
            ingredients.add(payload.toIngredient())
            writeAll(ingredients)
        }
    }

    suspend fun update(id: String, payload: IngredientPayload) = withContext(Dispatchers.IO) {
        lock.withLock {
            val ingredients = readAll().toMutableList()
            val index = ingredients.indexOfFirst { it.id == id }
            check(index >= 0) { "Ingredient not found" }
            val existing = ingredients[index]
            ingredients[index] = payload.toIngredient(id, existing.createdAt)
            writeAll(ingredients)
        }
    }

    suspend fun markAsConsumed(id: String, date: String) {
        updateStatus(id, "CONSUMED", date)
    }

    suspend fun markAsWasted(id: String, date: String) {
        updateStatus(id, "WASTED", date)
    }

    suspend fun getHistoricalData(): List<Ingredient> = withContext(Dispatchers.IO) {
        lock.withLock { readAll().filter { it.status != ACTIVE } }
    }

    private suspend fun updateStatus(id: String, status: String, date: String) =
        withContext(Dispatchers.IO) {
            lock.withLock {
                val ingredients = readAll().toMutableList()
                val index = ingredients.indexOfFirst { it.id == id }
                check(index >= 0) { "Ingredient not found" }
                ingredients[index] = ingredients[index].copy(
                    status = status,
                    resolvedDate = date
                )
                writeAll(ingredients)
            }
        }

    private fun readAll(): List<Ingredient> {
        val json = LocalStorage.preferences.getString(STORAGE_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return runCatching { gson.fromJson<List<Ingredient>>(json, type) }
            .getOrNull()
            .orEmpty()
    }

    private fun writeAll(ingredients: List<Ingredient>) {
        val saved = LocalStorage.preferences.edit()
            .putString(STORAGE_KEY, gson.toJson(ingredients))
            .commit()
        check(saved) { "Could not save ingredients on this device" }
    }

    private fun IngredientPayload.toIngredient(
        id: String = UUID.randomUUID().toString(),
        createdAt: String? = Instant.now().toString()
    ) = Ingredient(
        id = id,
        name = name,
        category = category,
        quantity = quantity,
        unit = unit,
        price = price,
        expiryDate = expiryDate,
        purchaseDate = purchaseDate,
        alertEnabled = alertEnabled,
        quickTag = quickTag,
        photoUrl = photoUrl,
        createdAt = createdAt,
        status = status ?: ACTIVE,
        resolvedDate = resolvedDate
    )
}
