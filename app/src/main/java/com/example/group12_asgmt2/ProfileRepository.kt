package com.example.group12_asgmt2

import io.github.jan.supabase.postgrest.from

object ProfileRepository {

    private val supabase = SupabaseManager.client
    private const val TABLE = "profiles"

    /** Fetch all profiles. */
    suspend fun getAll(): List<Profile> {
        return supabase.from(TABLE)
            .select()
            .decodeList<Profile>()
    }

    /** Fetch a profile by id. */
    suspend fun getById(id: String): Profile? {
        return try {
            supabase.from(TABLE)
                .select {
                    filter { eq("id", id) }
                }
                .decodeSingle<Profile>()
        } catch (e: Exception) {
            null
        }
    }

    /** Insert a new profile. */
    suspend fun insert(payload: ProfilePayload): Profile {
        return supabase.from(TABLE)
            .insert(payload) {
                select()
            }
            .decodeSingle<Profile>()
    }

    /** Update an existing profile. */
    suspend fun update(id: String, payload: ProfilePayload) {
        supabase.from(TABLE).update(payload) {
            filter { eq("id", id) }
        }
    }
}