package com.example.data

import android.content.Context
import android.os.Environment
import com.example.model.UnclassifiedFile
import com.example.model.UnclassifiedReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class FileScanner(private val context: Context) {

    // Common standard media and user file extensions that ARE classified
    private val recognizedExtensions = setOf(
        // Images
        "jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic", "heif", "ico",
        // Audio
        "mp3", "wav", "ogg", "m4a", "flac", "aac", "opus", "wma", "mid", "amr",
        // Video
        "mp4", "mkv", "avi", "mov", "3gp", "webm", "flv", "wmv", "m4v", "ts",
        // Documents
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "csv", "xml", "json", "html",
        // Archives & packages
        "zip", "rar", "7z", "tar", "gz", "bz2", "apk", "aab"
    )

    // Known temporary, partial, or orphaned extensions
    private val tempExtensions = setOf("tmp", "temp", "log", "cache", "swp", "chk")
    private val incompleteExtensions = setOf("part", "crdownload", "download", "incomplete", "0", "1")
    private val backupDumpExtensions = setOf("bak", "old", "dmp", "orig", "backup")

    /**
     * Scans accessible storage directories for pending and unclassified files.
     */
    suspend fun scanStorage(): List<UnclassifiedFile> = withContext(Dispatchers.IO) {
        val results = mutableListOf<UnclassifiedFile>()
        val directoriesToScan = mutableListOf<File>()

        // 1. Primary Download folder (frequent target for unclassified files)
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadDir != null && downloadDir.exists() && downloadDir.canRead()) {
            directoriesToScan.add(downloadDir)
        }

        // 2. Documents folder
        val docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (docDir != null && docDir.exists() && docDir.canRead()) {
            directoriesToScan.add(docDir)
        }

        // 3. Pictures / Screenshots custom files
        val picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (picDir != null && picDir.exists() && picDir.canRead()) {
            directoriesToScan.add(picDir)
        }

        // 4. General external storage root accessible files
        val extStorage = Environment.getExternalStorageDirectory()
        if (extStorage != null && extStorage.exists() && extStorage.canRead()) {
            directoriesToScan.add(extStorage)
        }

        // 5. App external cache and files directories
        context.getExternalFilesDirs(null)?.filterNotNull()?.forEach { dir ->
            if (dir.exists() && dir.canRead()) directoriesToScan.add(dir)
        }
        context.externalCacheDirs?.filterNotNull()?.forEach { dir ->
            if (dir.exists() && dir.canRead()) directoriesToScan.add(dir)
        }

        val visitedPaths = mutableSetOf<String>()

        for (dir in directoriesToScan) {
            scanDirectoryRecursively(dir, results, visitedPaths, depth = 0, maxDepth = 4)
        }

        // Sort largest to smallest by size
        results.sortByDescending { it.sizeBytes }
        results
    }

    private fun scanDirectoryRecursively(
        directory: File,
        results: MutableList<UnclassifiedFile>,
        visitedPaths: MutableSet<String>,
        depth: Int,
        maxDepth: Int
    ) {
        if (depth > maxDepth || !directory.exists() || !directory.canRead()) return

        // Safety: NEVER scan or touch system critical paths
        val pathLower = directory.absolutePath.lowercase(Locale.ROOT)
        if (isProtectedSystemPath(pathLower)) return

        val files = directory.listFiles() ?: return

        for (file in files) {
            try {
                if (file.isDirectory) {
                    if (!visitedPaths.contains(file.absolutePath)) {
                        visitedPaths.add(file.absolutePath)
                        scanDirectoryRecursively(file, results, visitedPaths, depth + 1, maxDepth)
                    }
                } else if (file.isFile && file.canRead()) {
                    val classification = classifyFile(file.name, file.length())
                    if (classification != null && !isProtectedSystemFile(file)) {
                        results.add(
                            UnclassifiedFile(
                                id = file.absolutePath,
                                name = file.name,
                                path = file.absolutePath,
                                extension = file.extension.ifEmpty { "None" },
                                sizeBytes = file.length(),
                                lastModified = file.lastModified(),
                                isSelected = false,
                                reason = classification
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // Skip unreadable or permission-denied files safely
            }
        }
    }

    /**
     * Determines if a file is unclassified, orphan, or pending.
     * Returns the reason if unclassified, or null if it's a recognized user file.
     */
    private fun classifyFile(fileName: String, sizeBytes: Long): UnclassifiedReason? {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)

        // 1. Files with no extension
        if (!fileName.contains('.') || ext.isEmpty()) {
            return UnclassifiedReason.NO_EXTENSION
        }

        // 2. Known temporary & cache files
        if (tempExtensions.contains(ext)) {
            return UnclassifiedReason.TEMP_FILE
        }

        // 3. Incomplete downloads / parts
        if (incompleteExtensions.contains(ext)) {
            return UnclassifiedReason.INCOMPLETE_PART
        }

        // 4. Backup & dumps
        if (backupDumpExtensions.contains(ext)) {
            return UnclassifiedReason.BACKUP_DUMP
        }

        // 5. Unrecognized extension and not a standard user document/media
        if (!recognizedExtensions.contains(ext)) {
            return UnclassifiedReason.UNKNOWN_BIN
        }

        // If it's a recognized extension, it's a normal file
        return null
    }

    /**
     * Safety checks: Protect system directories and critical OS files.
     */
    private fun isProtectedSystemPath(pathLower: String): Boolean {
        return pathLower.contains("/system") ||
                pathLower.contains("/proc") ||
                pathLower.contains("/sys") ||
                pathLower.contains("/data/system") ||
                pathLower.contains("/android/data") ||
                pathLower.contains("/android/obb") ||
                pathLower.contains("/lost.dir")
    }

    private fun isProtectedSystemFile(file: File): Boolean {
        val nameLower = file.name.lowercase(Locale.ROOT)
        return nameLower.startsWith(".nomedia") ||
                nameLower == "build.prop" ||
                nameLower.endsWith(".dex") ||
                nameLower.endsWith(".so") ||
                !file.canWrite()
    }

    /**
     * Deletes user-selected unclassified files safely.
     * Returns pair of (deletedCount, freedBytes).
     */
    suspend fun deleteFiles(filesToDelete: List<UnclassifiedFile>): Pair<Int, Long> =
        withContext(Dispatchers.IO) {
            var count = 0
            var freedBytes = 0L

            for (item in filesToDelete) {
                try {
                    val file = File(item.path)
                    if (file.exists() && file.isFile && file.canWrite() && !isProtectedSystemFile(file)) {
                        val size = file.length()
                        if (file.delete()) {
                            count++
                            freedBytes += size
                        }
                    }
                } catch (_: Exception) {
                    // Ignore deletion failures gracefully
                }
            }

            Pair(count, freedBytes)
        }
}
