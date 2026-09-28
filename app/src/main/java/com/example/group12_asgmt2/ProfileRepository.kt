package com.example.group12_asgmt2

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

object ProfileRepository {
    private const val STORAGE_KEY = "profiles"
    private val gson = Gson()
    private val lock = Mutex()

    suspend fun getAll(): List<Profile> = withContext(Dispatchers.IO) {
        lock.withLock { readAll() }
    }

    suspend fun getById(id: String): Profile? = withContext(Dispatchers.IO) {
        lock.withLock { readAll().firstOrNull { it.id == id } }
    }

    suspend fun insert(payload: ProfilePayload): Profile = withContext(Dispatchers.IO) {
        lock.withLock {
            val profile = Profile(
                id = UUID.randomUUID().toString(),
                username = payload.username,
                kitchenName = payload.kitchenName,
                createdAt = Instant.now().toString()
            )
            val profiles = readAll().toMutableList().apply { add(profile) }
            writeAll(profiles)
            profile
        }
    }

    suspend fun update(id: String, payload: ProfilePayload): Profile =
        withContext(Dispatchers.IO) {
            lock.withLock {
                val profiles = readAll().toMutableList()
                val index = profiles.indexOfFirst { it.id == id }
                check(index >= 0) { "Profile not found" }
                val updated = profiles[index].copy(
                    username = payload.username,
                    kitchenName = payload.kitchenName
                )
                profiles[index] = updated
                writeAll(profiles)
                updated
            }
        }

    private fun readAll(): List<Profile> {
        val json = LocalStorage.preferences.getString(STORAGE_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Profile>>() {}.type
        return runCatching { gson.fromJson<List<Profile>>(json, type) }
            .getOrNull()
            .orEmpty()
    }

    private fun writeAll(profiles: List<Profile>) {
        val saved = LocalStorage.preferences.edit()
            .putString(STORAGE_KEY, gson.toJson(profiles))
            .commit()
        check(saved) { "Could not save profile on this device" }
    }
}
