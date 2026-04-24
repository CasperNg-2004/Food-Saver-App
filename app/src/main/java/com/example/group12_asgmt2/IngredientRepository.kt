package com.example.group12_asgmt2

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object IngredientRepository {

    private val dbHelper by lazy { DatabaseHelper(MyApplication.appContext) }

    suspend fun getAll(): List<Ingredient> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_INGREDIENTS,
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_EXPIRY_DATE} ASC"
        )
        
        val list = mutableListOf<Ingredient>()
        with(cursor) {
            while (moveToNext()) {
                list.add(
                    Ingredient(
                        id = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        name = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
                        category = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
                        quantity = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY)),
                        unit = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNIT)),
                        price = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE)),
                        expiryDate = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPIRY_DATE)),
                        purchaseDate = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PURCHASE_DATE)),
                        alertEnabled = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALERT_ENABLED)) == 1,
                        quickTag = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUICK_TAG)),
                        photoUrl = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHOTO_URL)),
                        createdAt = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
                    )
                )
            }
        }
        cursor.close()
        list
    }

    suspend fun getByCategory(category: String): List<Ingredient> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_INGREDIENTS,
            null,
            "${DatabaseHelper.COLUMN_CATEGORY} = ?",
            arrayOf(category),
            null, null,
            "${DatabaseHelper.COLUMN_EXPIRY_DATE} ASC"
        )
        
        val list = mutableListOf<Ingredient>()
        with(cursor) {
            while (moveToNext()) {
                list.add(
                    Ingredient(
                        id = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        name = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
                        category = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
                        quantity = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY)),
                        unit = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNIT)),
                        price = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE)),
                        expiryDate = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPIRY_DATE)),
                        purchaseDate = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PURCHASE_DATE)),
                        alertEnabled = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALERT_ENABLED)) == 1,
                        quickTag = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUICK_TAG)),
                        photoUrl = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHOTO_URL)),
                        createdAt = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
                    )
                )
            }
        }
        cursor.close()
        list
    }

    suspend fun insert(payload: IngredientPayload) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, UUID.randomUUID().toString())
            put(DatabaseHelper.COLUMN_NAME, payload.name)
            put(DatabaseHelper.COLUMN_CATEGORY, payload.category)
            put(DatabaseHelper.COLUMN_QUANTITY, payload.quantity)
            put(DatabaseHelper.COLUMN_UNIT, payload.unit)
            put(DatabaseHelper.COLUMN_PRICE, payload.price)
            put(DatabaseHelper.COLUMN_EXPIRY_DATE, payload.expiryDate)
            put(DatabaseHelper.COLUMN_PURCHASE_DATE, payload.purchaseDate)
            put(DatabaseHelper.COLUMN_ALERT_ENABLED, if (payload.alertEnabled == true) 1 else 0)
            put(DatabaseHelper.COLUMN_QUICK_TAG, payload.quickTag)
            put(DatabaseHelper.COLUMN_PHOTO_URL, payload.photoUrl)
            put(DatabaseHelper.COLUMN_CREATED_AT, System.currentTimeMillis().toString())
        }
        db.insert(DatabaseHelper.TABLE_INGREDIENTS, null, values)
    }

    suspend fun update(id: String, payload: IngredientPayload) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, payload.name)
            put(DatabaseHelper.COLUMN_CATEGORY, payload.category)
            put(DatabaseHelper.COLUMN_QUANTITY, payload.quantity)
            put(DatabaseHelper.COLUMN_UNIT, payload.unit)
            put(DatabaseHelper.COLUMN_PRICE, payload.price)
            put(DatabaseHelper.COLUMN_EXPIRY_DATE, payload.expiryDate)
            put(DatabaseHelper.COLUMN_PURCHASE_DATE, payload.purchaseDate)
            put(DatabaseHelper.COLUMN_ALERT_ENABLED, if (payload.alertEnabled == true) 1 else 0)
            put(DatabaseHelper.COLUMN_QUICK_TAG, payload.quickTag)
            put(DatabaseHelper.COLUMN_PHOTO_URL, payload.photoUrl)
        }
        db.update(DatabaseHelper.TABLE_INGREDIENTS, values, "${DatabaseHelper.COLUMN_ID} = ?", arrayOf(id))
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_INGREDIENTS, "${DatabaseHelper.COLUMN_ID} = ?", arrayOf(id))
    }
}
