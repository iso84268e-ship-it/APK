package com.example.data

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.example.model.PrivacyAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivacyMonitor(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val appOpsManager =
        context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager

    /**
     * Retrieves all installed applications that declare or hold CAMERA and/or RECORD_AUDIO permissions.
     * Checks real-time op activity if supported by Android API (API 30+).
     */
    suspend fun getPrivacyApps(): List<PrivacyAppInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PrivacyAppInfo>()
        val installedPackages: List<PackageInfo> = try {
            packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        } catch (_: Exception) {
            emptyList()
        }

        for (pkg in installedPackages) {
            val requestedPerms = pkg.requestedPermissions ?: continue
            val hasCamera = requestedPerms.contains(android.Manifest.permission.CAMERA)
            val hasMic = requestedPerms.contains(android.Manifest.permission.RECORD_AUDIO)

            if (!hasCamera && !hasMic) continue

            val appInfo = pkg.applicationInfo ?: continue
            val appLabel = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg.packageName
            }

            val icon = try {
                packageManager.getApplicationIcon(appInfo)
            } catch (_: Exception) {
                null
            }

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            // Check live active status using AppOpsManager on Android 11+
            var isCameraActive = false
            var isMicActive = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && appOpsManager != null) {
                try {
                    val uid = appInfo.uid
                    val packageName = pkg.packageName
                    isCameraActive = hasCamera && appOpsManager.isOpActive(
                        AppOpsManager.OPSTR_CAMERA,
                        uid,
                        packageName
                    )
                    isMicActive = hasMic && appOpsManager.isOpActive(
                        AppOpsManager.OPSTR_RECORD_AUDIO,
                        uid,
                        packageName
                    )
                } catch (_: Exception) {
                    // Ignored if app-ops active query is restricted
                }
            }

            // Note on timestamps: On Android 12+, cross-app historical timestamps are strictly
            // restricted to the system Privacy Dashboard (PermissionController).
            // We record lastUpdateTime of the package as reliable metadata when historical op is restricted.
            val lastTime = if (pkg.lastUpdateTime > 0) pkg.lastUpdateTime else null

            results.add(
                PrivacyAppInfo(
                    packageName = pkg.packageName,
                    appName = appLabel,
                    icon = icon,
                    hasCameraPermission = hasCamera,
                    hasMicPermission = hasMic,
                    isCameraActiveNow = isCameraActive,
                    isMicActiveNow = isMicActive,
                    lastAccessTime = lastTime,
                    isSystemApp = isSystem
                )
            )
        }

        // Sort: Active apps first, then non-system user apps, then alphabetically
        results.sortWith(
            compareByDescending<PrivacyAppInfo> { it.isCameraActiveNow || it.isMicActiveNow }
                .thenBy { it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )
        results
    }

    /**
     * Creates an intent to launch Samsung's / Android's official Privacy Dashboard
     * where the user can view the full 24-hour historical timeline.
     */
    fun createOpenPrivacyDashboardIntent(): Intent {
        val intentsToTry = listOf(
            Intent("android.intent.action.REVIEW_PERMISSION_USAGE"),
            Intent(Settings.ACTION_PRIVACY_SETTINGS),
            Intent("android.settings.PRIVACY_SETTINGS"),
            Intent(Settings.ACTION_APPLICATION_SETTINGS)
        )

        for (intent in intentsToTry) {
            if (intent.resolveActivity(packageManager) != null) {
                return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
