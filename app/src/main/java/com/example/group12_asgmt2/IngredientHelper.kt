package com.example.group12_asgmt2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IngredientCallback {
    fun onSuccess(ingredients: List<Ingredient>)
    fun onError(e: Exception)
}

object IngredientHelper {
    @JvmStatic
    fun fetchAllIngredients(callback: IngredientCallback): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                val list = IngredientRepository.getAll()
                withContext(Dispatchers.Main) {
                    callback.onSuccess(list)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError(e)
                }
            }
        }
    }
}
