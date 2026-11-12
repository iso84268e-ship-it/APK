package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.model.MemoryPressure
import com.example.model.RamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RamManager(private val context: Context) {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    /**
     * Reads real-time RAM statistics using Android's ActivityManager.MemoryInfo API.
     */
    fun getRamInfo(): RamInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val total = if (memoryInfo.totalMem > 0) memoryInfo.totalMem else 4L * 1024 * 1024 * 1024 // Default fallback for Tab A9
        val available = memoryInfo.availMem
        val used = (total - available).coerceAtLeast(0L)
        val percentage = ((used.toDouble() / total.toDouble()) * 100).toInt().coerceIn(0, 100)

        val pressure = when {
            percentage < 65 -> MemoryPressure.OPTIMAL
            percentage <= 82 -> MemoryPressure.WARNING
            else -> MemoryPressure.CRITICAL
        }

        return RamInfo(
            totalBytes = total,
            availableBytes = available,
            usedBytes = used,
            usagePercentage = percentage,
            isLowMemory = memoryInfo.lowMemory,
            thresholdBytes = memoryInfo.threshold,
            pressureLevel = pressure
        )
    }

    /**
     * Emits RAM updates every [intervalMs] milliseconds while active.
     */
    fun observeRamInfo(intervalMs: Long = 2000L): Flow<RamInfo> = flow {
        while (true) {
            emit(getRamInfo())
            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Performs a safe, Android-supported memory cleanup:
     * 1. Kills background processes for eligible user apps via ActivityManager.killBackgroundProcesses.
     * 2. Requests JVM garbage collection for cached resources.
     * 3. Calculates the actual freed RAM delta without fake numbers.
     */
    suspend fun cleanRam(): CleanResult = withContext(Dispatchers.IO) {
        val before = getRamInfo()

        // 1. Attempt to kill eligible background tasks if permission is granted
        val pm = context.packageManager
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val currentPkg = context.packageName
            for (app in installedApps) {
                // Do not kill system apps or Tab Cleaner itself
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (!isSystem && app.packageName != currentPkg) {
                    activityManager?.killBackgroundProcesses(app.packageName)
                }
            }
        } catch (_: Exception) {
            // Permission or security exception handled gracefully
        }

        // 2. Request JVM garbage collection
        System.gc()
        Runtime.getRuntime().gc()

        // Brief delay to let the OS reclaim freed memory pages
        delay(600L)

        val after = getRamInfo()
        val freed = (after.availableBytes - before.availableBytes).coerceAtLeast(0L)

        CleanResult(
            beforeInfo = before,
            afterInfo = after,
            freedBytes = freed
        )
    }

    data class CleanResult(
        val beforeInfo: RamInfo,
        val afterInfo: RamInfo,
        val freedBytes: Long
    )
}
