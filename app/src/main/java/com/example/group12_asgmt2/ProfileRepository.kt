package com.example.group12_asgmt2

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProfileRepository {
    private val table = SupabaseManager.client.postgrest["profiles"]

    suspend fun getAll(): List<Profile> = withContext(Dispatchers.IO) {
        table.select(Columns.ALL).decodeList<Profile>()
    }

    suspend fun getById(id: String): Profile? = withContext(Dispatchers.IO) {
        table.select(Columns.ALL) {
            filter { eq("id", id) }
        }.decodeSingleOrNull<Profile>()
    }

    suspend fun insert(payload: ProfilePayload): Profile = withContext(Dispatchers.IO) {
        table.insert(payload) {
            select() // Return the inserted row so we can get the generated id
        }.decodeSingle<Profile>()
    }

    suspend fun update(id: String, payload: ProfilePayload): Profile = withContext(Dispatchers.IO) {
        table.update(payload) {
            filter { eq("id", id) }
            select() // Return the updated row
        }.decodeSingle<Profile>()
    }
}
