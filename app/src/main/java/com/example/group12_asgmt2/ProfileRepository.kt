package com.example.group12_asgmt2

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object ProfileRepository {

    private val dbHelper by lazy { DatabaseHelper(MyApplication.appContext) }

    suspend fun getAll(): List<Profile> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_PROFILES,
            null, null, null, null, null, null
        )
        
        val list = mutableListOf<Profile>()
        with(cursor) {
            while (moveToNext()) {
                list.add(
                    Profile(
                        id = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROFILE_ID)),
                        username = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
                        kitchenName = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_KITCHEN_NAME))
                    )
                )
            }
        }
        cursor.close()
        list
    }

    suspend fun getById(id: String): Profile? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_PROFILES,
            null,
            "${DatabaseHelper.COLUMN_PROFILE_ID} = ?",
            arrayOf(id),
            null, null, null
        )
        
        var profile: Profile? = null
        with(cursor) {
            if (moveToFirst()) {
                profile = Profile(
                    id = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROFILE_ID)),
                    username = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
                    kitchenName = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_KITCHEN_NAME))
                )
            }
        }
        cursor.close()
        profile
    }

    suspend fun insert(payload: ProfilePayload): Profile = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val newId = UUID.randomUUID().toString()
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PROFILE_ID, newId)
            put(DatabaseHelper.COLUMN_USERNAME, payload.username)
            put(DatabaseHelper.COLUMN_KITCHEN_NAME, payload.kitchenName)
        }
        db.insert(DatabaseHelper.TABLE_PROFILES, null, values)
        Profile(id = newId, username = payload.username, kitchenName = payload.kitchenName)
    }

    suspend fun update(id: String, payload: ProfilePayload): Profile = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USERNAME, payload.username)
            put(DatabaseHelper.COLUMN_KITCHEN_NAME, payload.kitchenName)
        }
        db.update(DatabaseHelper.TABLE_PROFILES, values, "${DatabaseHelper.COLUMN_PROFILE_ID} = ?", arrayOf(id))
        Profile(id = id, username = payload.username, kitchenName = payload.kitchenName)
    }
}
