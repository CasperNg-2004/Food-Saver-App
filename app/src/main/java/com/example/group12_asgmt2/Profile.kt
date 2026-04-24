package com.example.group12_asgmt2

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Profile(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val id: String? = null,
    val username: String,
    @SerialName("kitchen_name")
    val kitchenName: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class ProfilePayload(
    val username: String,
    @SerialName("kitchen_name")
    val kitchenName: String
)
