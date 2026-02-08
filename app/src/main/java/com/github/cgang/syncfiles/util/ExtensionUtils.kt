package com.github.cgang.syncfiles.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FileSizeUtils {
    private val units = arrayOf("B", "KB", "MB", "GB", "TB")

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(
            Locale.US,
            "%.1f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }
}

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun parseIsoDate(isoString: String?): Date {
        if (isoString == null) return Date()
        return try {
            isoFormat.parse(isoString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    fun formatDisplayDate(date: Date): String {
        return displayFormat.format(date)
    }

    fun Date.toIsoString(): String {
        return isoFormat.format(this)
    }
}
