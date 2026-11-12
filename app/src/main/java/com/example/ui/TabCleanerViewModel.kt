package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileScanner
import com.example.data.PrivacyMonitor
import com.example.data.RamManager
import com.example.model.MemoryPressure
import com.example.model.PrivacyAppInfo
import com.example.model.RamInfo
import com.example.model.TabDestination
import com.example.model.UnclassifiedFile
import com.example.model.UnclassifiedReason
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FileFilter {
    ALL,
    NO_EXTENSION,
    TEMP,
    INCOMPLETE
}

enum class PrivacyFilter {
    ALL,
    CAMERA,
    MIC,
    ACTIVE_NOW
}

class TabCleanerViewModel(application: Application) : AndroidViewModel(application) {

    private val ramManager = RamManager(application)
    private val fileScanner = FileScanner(application)
    val privacyMonitor = PrivacyMonitor(application)

    // Current Screen
    private val _currentTab = MutableStateFlow(TabDestination.RAM)
    val currentTab: StateFlow<TabDestination> = _currentTab.asStateFlow()

    // RAM State
    private val _ramInfo = MutableStateFlow(ramManager.getRamInfo())
    val ramInfo: StateFlow<RamInfo> = _ramInfo.asStateFlow()

    private val _isCleaningRam = MutableStateFlow(false)
    val isCleaningRam: StateFlow<Boolean> = _isCleaningRam.asStateFlow()

    private val _ramCleanResultMsg = MutableStateFlow<String?>(null)
    val ramCleanResultMsg: StateFlow<String?> = _ramCleanResultMsg.asStateFlow()

    // Files State
    private val _allFiles = MutableStateFlow<List<UnclassifiedFile>>(emptyList())
    val allFiles: StateFlow<List<UnclassifiedFile>> = _allFiles.asStateFlow()

    private val _isScanningFiles = MutableStateFlow(false)
    val isScanningFiles: StateFlow<Boolean> = _isScanningFiles.asStateFlow()

    private val _fileFilter = MutableStateFlow(FileFilter.ALL)
    val fileFilter: StateFlow<FileFilter> = _fileFilter.asStateFlow()

    val filteredFiles: StateFlow<List<UnclassifiedFile>> =
        combine(_allFiles, _fileFilter) { files, filter ->
            when (filter) {
                FileFilter.ALL -> files
                FileFilter.NO_EXTENSION -> files.filter { it.reason == UnclassifiedReason.NO_EXTENSION }
                FileFilter.TEMP -> files.filter { it.reason == UnclassifiedReason.TEMP_FILE || it.reason == UnclassifiedReason.BACKUP_DUMP }
                FileFilter.INCOMPLETE -> files.filter { it.reason == UnclassifiedReason.INCOMPLETE_PART || it.reason == UnclassifiedReason.UNKNOWN_BIN }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFilesSize: StateFlow<Long> = _allFiles.combine(_allFiles) { files, _ ->
        files.sumOf { it.sizeBytes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val selectedFilesCount: StateFlow<Int> = _allFiles.combine(_allFiles) { files, _ ->
        files.count { it.isSelected }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val selectedFilesSize: StateFlow<Long> = _allFiles.combine(_allFiles) { files, _ ->
        files.filter { it.isSelected }.sumOf { it.sizeBytes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()

    private val _isDeletingFiles = MutableStateFlow(false)
    val isDeletingFiles: StateFlow<Boolean> = _isDeletingFiles.asStateFlow()

    private val _fileActionNotice = MutableStateFlow<String?>(null)
    val fileActionNotice: StateFlow<String?> = _fileActionNotice.asStateFlow()

    private val _hasStoragePermission = MutableStateFlow(true)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    // Privacy State
    private val _allPrivacyApps = MutableStateFlow<List<PrivacyAppInfo>>(emptyList())
    private val _isLoadingPrivacy = MutableStateFlow(false)
    val isLoadingPrivacy: StateFlow<Boolean> = _isLoadingPrivacy.asStateFlow()

    private val _privacyFilter = MutableStateFlow(PrivacyFilter.ALL)
    val privacyFilter: StateFlow<PrivacyFilter> = _privacyFilter.asStateFlow()

    val filteredPrivacyApps: StateFlow<List<PrivacyAppInfo>> =
        combine(_allPrivacyApps, _privacyFilter) { apps, filter ->
            when (filter) {
                PrivacyFilter.ALL -> apps
                PrivacyFilter.CAMERA -> apps.filter { it.hasCameraPermission }
                PrivacyFilter.MIC -> apps.filter { it.hasMicPermission }
                PrivacyFilter.ACTIVE_NOW -> apps.filter { it.isCameraActiveNow || it.isMicActiveNow }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePrivacyCount: StateFlow<Int> = _allPrivacyApps.combine(_allPrivacyApps) { apps, _ ->
        apps.count { it.isCameraActiveNow || it.isMicActiveNow }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Start periodic RAM observation
        viewModelScope.launch {
            ramManager.observeRamInfo(2500L).collect { info ->
                if (!_isCleaningRam.value) {
                    _ramInfo.value = info
                }
            }
        }
        // Initial scan of files and privacy
        scanFiles()
        loadPrivacyApps()
    }

    fun selectTab(destination: TabDestination) {
        _currentTab.value = destination
        if (destination == TabDestination.RAM) {
            refreshRam()
        } else if (destination == TabDestination.PRIVACY) {
            loadPrivacyApps()
        }
    }

    fun refreshRam() {
        _ramInfo.value = ramManager.getRamInfo()
    }

    fun cleanRam() {
        if (_isCleaningRam.value) return
        viewModelScope.launch {
            _isCleaningRam.value = true
            _ramCleanResultMsg.value = null

            val result = ramManager.cleanRam()
            _ramInfo.value = result.afterInfo
            _isCleaningRam.value = false

            val freedFormatted = FormatUtils.formatBytes(result.freedBytes)
            _ramCleanResultMsg.value = if (result.freedBytes > 0) {
                "تم تحرير $freedFormatted من الذاكرة المؤقتة للعمليات الخلفية بأمان."
            } else {
                "الذاكرة العشوائية مُحسّنة بالفعل ومستقرة؛ يحتفظ أندرويد بالذاكرة اللازمة للواجهة."
            }
        }
    }

    fun dismissRamNotice() {
        _ramCleanResultMsg.value = null
    }

    fun scanFiles() {
        if (_isScanningFiles.value) return
        viewModelScope.launch {
            _isScanningFiles.value = true
            _fileActionNotice.value = null
            val discovered = fileScanner.scanStorage()
            _allFiles.value = discovered
            _isScanningFiles.value = false
        }
    }

    fun scanCustomTree(treeUri: Uri) {
        scanFiles()
    }

    fun setFileFilter(filter: FileFilter) {
        _fileFilter.value = filter
    }

    fun toggleFileSelection(fileId: String) {
        _allFiles.value = _allFiles.value.map {
            if (it.id == fileId) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun selectAllFiles(select: Boolean) {
        val currentFilteredIds = filteredFiles.value.map { it.id }.toSet()
        _allFiles.value = _allFiles.value.map { file ->
            if (currentFilteredIds.contains(file.id)) {
                file.copy(isSelected = select)
            } else {
                file
            }
        }
    }

    fun requestDeleteConfirmation() {
        if (selectedFilesCount.value > 0) {
            _showDeleteConfirmDialog.value = true
        }
    }

    fun dismissDeleteDialog() {
        _showDeleteConfirmDialog.value = false
    }

    fun executeDeleteSelected() {
        _showDeleteConfirmDialog.value = false
        val toDelete = _allFiles.value.filter { it.isSelected }
        if (toDelete.isEmpty()) return

        viewModelScope.launch {
            _isDeletingFiles.value = true
            val (deletedCount, freedBytes) = fileScanner.deleteFiles(toDelete)
            _isDeletingFiles.value = false

            // Update remaining files
            val deletedIds = toDelete.take(deletedCount).map { it.id }.toSet()
            _allFiles.value = _allFiles.value.filterNot { deletedIds.contains(it.id) }

            val freedStr = FormatUtils.formatBytes(freedBytes)
            _fileActionNotice.value = "تم حذف $deletedCount ملف بنجاح وتحرير $freedStr من مساحة التخزين."
        }
    }

    fun dismissFileNotice() {
        _fileActionNotice.value = null
    }

    fun setStoragePermissionGranted(granted: Boolean) {
        _hasStoragePermission.value = granted
        if (granted) {
            scanFiles()
        }
    }

    fun loadPrivacyApps() {
        if (_isLoadingPrivacy.value) return
        viewModelScope.launch {
            _isLoadingPrivacy.value = true
            val apps = privacyMonitor.getPrivacyApps()
            _allPrivacyApps.value = apps
            _isLoadingPrivacy.value = false
        }
    }

    fun setPrivacyFilter(filter: PrivacyFilter) {
        _privacyFilter.value = filter
    }
}
