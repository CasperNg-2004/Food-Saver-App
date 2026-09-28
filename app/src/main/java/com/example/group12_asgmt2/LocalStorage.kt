package com.example.group12_asgmt2

import android.content.Context
import android.content.SharedPreferences

object LocalStorage {
    private const val PREFERENCES_NAME = "pantry_local_storage"
    private var sharedPreferences: SharedPreferences? = null

    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    val preferences: SharedPreferences
        get() = checkNotNull(sharedPreferences) {
            "LocalStorage must be initialized by PantryApplication"
        }
}
