package com.example.group12_asgmt2

data class DashboardSummary(
    val kitchenName: String,
    val totalItems: Int,
    val freshCount: Int,
    val nearExpiryCount: Int,
    val expiredCount: Int,
    val usageRate: Int,
    val discardRate: Int,
    val wastedCost: Double,
    val weeklySaved: Double
)