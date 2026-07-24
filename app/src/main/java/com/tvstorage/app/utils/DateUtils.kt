package com.tvstorage.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun getFormattedTimeSince(timestamp: Long): TimeParts {
        val now = System.currentTimeMillis()
        val diff = if (timestamp > now) 0 else now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return TimeParts(
            days = days,
            hours = hours % 24,
            minutes = minutes % 60
        )
    }

    data class TimeParts(
        val days: Long,
        val hours: Long,
        val minutes: Long
    )

    fun getDaysSince(timestamp: Long): Long {
        val now = System.currentTimeMillis()
        if (timestamp > now) return 0
        val diff = now - timestamp
        return diff / (1000 * 60 * 60 * 24)
    }

    fun getBillingDays(timestamp: Long): Long {
        val days = getDaysSince(timestamp)
        return days + 1 // Первый день считается сразу
    }

    fun calculateTotalCost(dailyCost: Double, days: Long): Double {
        return dailyCost * days
    }
}
