package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 ميجابايت"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> String.format(Locale.getDefault(), "%.2f جيجابايت", gb)
            mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f ميجابايت", mb)
            else -> String.format(Locale.getDefault(), "%.1f كيلوبايت", kb)
        }
    }

    fun formatBytesDual(bytes: Long): String {
        if (bytes <= 0) return "0 MB (0 GB)"
        val mb = bytes / (1024.0 * 1024.0)
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return String.format(Locale.getDefault(), "%.1f ميجابايت (%.2f جيجابايت)", mb, gb)
    }

    fun formatGigabytesOnly(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return String.format(Locale.getDefault(), "%.2f جيجابايت", gb)
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "غير محدد"
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
