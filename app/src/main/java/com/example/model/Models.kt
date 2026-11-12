package com.example.model

import android.graphics.drawable.Drawable

/**
 * Represents RAM memory stats retrieved from ActivityManager.
 */
data class RamInfo(
    val totalBytes: Long = 0L,
    val availableBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usagePercentage: Int = 0,
    val isLowMemory: Boolean = false,
    val thresholdBytes: Long = 0L,
    val pressureLevel: MemoryPressure = MemoryPressure.OPTIMAL
)

enum class MemoryPressure {
    OPTIMAL,   // < 65%
    WARNING,   // 65% - 82%
    CRITICAL   // > 82%
}

/**
 * Represents a pending or unclassified file found in device storage.
 */
data class UnclassifiedFile(
    val id: String,
    val name: String,
    val path: String,
    val extension: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isSelected: Boolean = false,
    val reason: UnclassifiedReason = UnclassifiedReason.NO_EXTENSION
)

enum class UnclassifiedReason {
    NO_EXTENSION,      // ملف بدون امتداد
    TEMP_FILE,         // ملف مؤقت (.tmp, .temp, .log, .cache)
    INCOMPLETE_PART,   // ملف تحميل غير مكتمل (.part, .crdownload)
    BACKUP_DUMP,       // ملف نسخ قديم أو تفريغ (.bak, .old, .dmp)
    UNKNOWN_BIN        // امتداد غير معروف أو ثنائي غير مصنف
}

/**
 * Represents an installed app with Camera or Microphone permissions.
 */
data class PrivacyAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val hasCameraPermission: Boolean = false,
    val hasMicPermission: Boolean = false,
    val isCameraActiveNow: Boolean = false,
    val isMicActiveNow: Boolean = false,
    val lastAccessTime: Long? = null,
    val isSystemApp: Boolean = false
)

/**
 * Three main navigation sections of Tab Cleaner.
 */
enum class TabDestination {
    RAM,
    FILES,
    PRIVACY
}
