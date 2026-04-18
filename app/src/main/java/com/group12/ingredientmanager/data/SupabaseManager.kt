package com.group12.ingredientmanager.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {

    private const val SUPABASE_URL = "https://rasieiwetwwdbbamdeai.supabase.co"
    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
        "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJhc2llaXdldHd3ZGJiYW1kZWFpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY0ODY4MTMsImV4cCI6MjA5MjA2MjgxM30." +
        "8G9vHehZmcE83Z7oym9-7tKf3wg-hswn-eD6qEWMsa4"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
}
