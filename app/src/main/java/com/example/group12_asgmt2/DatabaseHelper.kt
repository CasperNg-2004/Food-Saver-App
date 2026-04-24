package com.example.group12_asgmt2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pantry_database.db"
        private const val DATABASE_VERSION = 1

        // Ingredients Table
        const val TABLE_INGREDIENTS = "ingredients"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_UNIT = "unit"
        const val COLUMN_PRICE = "price"
        const val COLUMN_EXPIRY_DATE = "expiry_date"
        const val COLUMN_PURCHASE_DATE = "purchase_date"
        const val COLUMN_ALERT_ENABLED = "alert_enabled"
        const val COLUMN_QUICK_TAG = "quick_tag"
        const val COLUMN_PHOTO_URL = "photo_url"
        const val COLUMN_CREATED_AT = "created_at"

        // Profiles Table
        const val TABLE_PROFILES = "profiles"
        const val COLUMN_PROFILE_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_KITCHEN_NAME = "kitchen_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createIngredientsTable = """
            CREATE TABLE $TABLE_INGREDIENTS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_QUANTITY REAL NOT NULL,
                $COLUMN_UNIT TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_EXPIRY_DATE TEXT NOT NULL,
                $COLUMN_PURCHASE_DATE TEXT,
                $COLUMN_ALERT_ENABLED INTEGER,
                $COLUMN_QUICK_TAG TEXT,
                $COLUMN_PHOTO_URL TEXT,
                $COLUMN_CREATED_AT TEXT
            )
        """.trimIndent()

        val createProfilesTable = """
            CREATE TABLE $TABLE_PROFILES (
                $COLUMN_PROFILE_ID TEXT PRIMARY KEY,
                $COLUMN_USERNAME TEXT,
                $COLUMN_KITCHEN_NAME TEXT
            )
        """.trimIndent()

        db.execSQL(createIngredientsTable)
        db.execSQL(createProfilesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INGREDIENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILES")
        onCreate(db)
    }
}
