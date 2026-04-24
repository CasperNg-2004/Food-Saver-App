package com.example.group12_asgmt2

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {

    private val DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy")

    /** Returns the number of days until the expiry date (negative if already expired). */
    fun daysUntilExpiry(expiryDate: String): Long {
        return try {
            val expiry = LocalDate.parse(expiryDate, DB_FORMAT)
            ChronoUnit.DAYS.between(LocalDate.now(), expiry)
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    /** Converts a yyyy-MM-dd string to a user-friendly "dd MMM yyyy" format. */
    fun formatForDisplay(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr, DB_FORMAT)
            date.format(DISPLAY_FORMAT)
        } catch (e: Exception) {
            dateStr
        }
    }

    /** Converts a LocalDate to the yyyy-MM-dd string for DB storage. */
    fun formatForDb(date: LocalDate): String = date.format(DB_FORMAT)

    /**
     * Returns an expiry status:
     * EXPIRED  — expiry date has passed
     * SOON     — expiring within 7 days
     * FRESH    — more than 7 days remaining
     */
    enum class ExpiryStatus { EXPIRED, SOON, FRESH }

    fun getExpiryStatus(expiryDate: String): ExpiryStatus {
        val days = daysUntilExpiry(expiryDate)
        return when {
            days < 0   -> ExpiryStatus.EXPIRED
            days <= 7  -> ExpiryStatus.SOON
            else       -> ExpiryStatus.FRESH
        }
    }
}
