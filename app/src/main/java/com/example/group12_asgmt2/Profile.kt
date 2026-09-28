package com.example.group12_asgmt2

data class Profile(
    val id: String? = null,
    val username: String,
    val kitchenName: String,
    val createdAt: String? = null
)

data class ProfilePayload(
    val username: String,
    val kitchenName: String
)
