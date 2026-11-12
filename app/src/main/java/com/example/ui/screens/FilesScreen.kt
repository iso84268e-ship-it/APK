package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UnclassifiedFile
import com.example.model.UnclassifiedReason
import com.example.ui.FileFilter
import com.example.ui.TabCleanerViewModel
import com.example.ui.components.OneUiCard
import com.example.ui.theme.SamsungBlue
import com.example.ui.theme.SamsungBlueContainer
import com.example.ui.theme.SamsungCardBorder
import com.example.ui.theme.SamsungSurface
import com.example.ui.theme.SamsungSurfaceVariant
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun FilesScreen(
    viewModel: TabCleanerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val files by viewModel.filteredFiles.collectAsState()
    val isScanning by viewModel.isScanningFiles.collectAsState()
    val totalSize by viewModel.totalFilesSize.collectAsState()
    val selectedCount by viewModel.selectedFilesCount.collectAsState()
    val selectedSize by viewModel.selectedFilesSize.collectAsState()
    val showConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()
    val isDeleting by viewModel.isDeletingFiles.collectAsState()
    val actionNotice by viewModel.fileActionNotice.collectAsState()
    val currentFilter by viewModel.fileFilter.collectAsState()

    var selectedPreviewFile by remember { mutableStateOf<UnclassifiedFile?>(null) }

    // Storage Access Framework (SAF) tree picker
    val openDocumentTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            viewModel.scanCustomTree(it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 850.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = SamsungBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.files_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.files_subtitle),
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            Row {
                // Pick SAF directory
                IconButton(
                    onClick = { openDocumentTreeLauncher.launch(null) },
                    enabled = !isScanning && !isDeleting,
                    modifier = Modifier
                        .background(SamsungSurfaceVariant, CircleShape)
                        .testTag("saf_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "تحديد مجلد مخصص",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Rescan
                IconButton(
                    onClick = { viewModel.scanFiles() },
                    enabled = !isScanning && !isDeleting,
                    modifier = Modifier
                        .background(SamsungSurfaceVariant, CircleShape)
                        .testTag("rescan_files_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.files_scan_button),
                        tint = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Notice Banner if present
        AnimatedVisibility(
            visible = actionNotice != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            actionNotice?.let { notice ->
                OneUiCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 850.dp)
                        .padding(bottom = 14.dp),
                    backgroundColor = StatusGreenContainer,
                    borderColor = StatusGreen.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = notice,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissFileNotice() }) {
                            Text("✕", color = TextMuted)
                        }
                    }
                }
            }
        }

        // Summary Bar Card (Total Size in MB and GB)
        OneUiCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 850.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إجمالي الملفات غير المصنفة",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatBytesDual(totalSize),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalSize > 0) StatusOrange else StatusGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "عدد الملفات المكتشفة",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${files.size} ملف",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter and Selection Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 850.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentFilter == FileFilter.ALL,
                    onClick = { viewModel.setFileFilter(FileFilter.ALL) },
                    label = { Text("الكل", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SamsungBlue,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = currentFilter == FileFilter.NO_EXTENSION,
                    onClick = { viewModel.setFileFilter(FileFilter.NO_EXTENSION) },
                    label = { Text("بدون امتداد", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SamsungBlue,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = currentFilter == FileFilter.TEMP,
                    onClick = { viewModel.setFileFilter(FileFilter.TEMP) },
                    label = { Text("ملفات مؤقتة", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SamsungBlue,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = currentFilter == FileFilter.INCOMPLETE,
                    onClick = { viewModel.setFileFilter(FileFilter.INCOMPLETE) },
                    label = { Text("أجزاء / غير مكتمل", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SamsungBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Row {
                TextButton(
                    onClick = { viewModel.selectAllFiles(true) },
                    enabled = files.isNotEmpty()
                ) {
                    Text(stringResource(R.string.files_select_all), fontSize = 12.sp, color = SamsungBlue)
                }
                TextButton(
                    onClick = { viewModel.selectAllFiles(false) },
                    enabled = selectedCount > 0
                ) {
                    Text(stringResource(R.string.files_deselect_all), fontSize = 12.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Files List or Empty / Scanning State
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 850.dp)
        ) {
            when {
                isScanning -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = SamsungBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.files_scanning),
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                    }
                }

                files.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(SamsungSurfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.files_empty_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.files_empty_desc),
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(files, key = { it.id }) { item ->
                            FileItemCard(
                                file = item,
                                onToggleSelect = { viewModel.toggleFileSelection(item.id) },
                                onPreviewClick = { selectedPreviewFile = item }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Action Bar: Delete Button
        if (selectedCount > 0) {
            OneUiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 850.dp),
                backgroundColor = SamsungSurfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "المحدد للحذف: $selectedCount ملف",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "المساحة الموفرة: ${FormatUtils.formatBytes(selectedSize)}",
                            fontSize = 12.sp,
                            color = StatusOrange
                        )
                    }

                    Button(
                        onClick = { viewModel.requestDeleteConfirmation() },
                        enabled = !isDeleting,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusRed,
                            contentColor = Color.White,
                            disabledContainerColor = StatusRedContainer
                        ),
                        modifier = Modifier.testTag("delete_selected_files_button")
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حذف المحدد",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog before deletion
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.files_confirm_delete_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.files_confirm_delete_msg,
                        selectedCount,
                        FormatUtils.formatBytes(selectedSize)
                    ),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeDeleteSelected() },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text(stringResource(R.string.files_confirm_action), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text(stringResource(R.string.files_cancel_action), color = TextPrimary)
                }
            },
            containerColor = SamsungSurface,
            shape = RoundedCornerShape(22.dp)
        )
    }

    // Preview File Details Dialog
    selectedPreviewFile?.let { file ->
        AlertDialog(
            onDismissRequest = { selectedPreviewFile = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = SamsungBlue,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "تفاصيل الملف",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الاسم: ${file.name}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("المسار: ${file.path}", fontSize = 12.sp, color = TextMuted)
                    Text("الحجم: ${FormatUtils.formatBytesDual(file.sizeBytes)}", fontSize = 13.sp, color = StatusOrange)
                    Text("الامتداد: ${file.extension}", fontSize = 13.sp)
                    Text("تاريخ التعديل: ${FormatUtils.formatDate(file.lastModified)}", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = "التصنيف: " + when (file.reason) {
                            UnclassifiedReason.NO_EXTENSION -> "ملف بدون امتداد معروف"
                            UnclassifiedReason.TEMP_FILE -> "ملف مؤقت مهمل (.tmp, .log, .cache)"
                            UnclassifiedReason.INCOMPLETE_PART -> "تنزيل مجزأ غير مكتمل (.part, .crdownload)"
                            UnclassifiedReason.BACKUP_DUMP -> "ملف نسخ احتياطي قديم (.bak, .old)"
                            UnclassifiedReason.UNKNOWN_BIN -> "امتداد ثنائي غير معروف"
                        },
                        fontSize = 12.sp,
                        color = SamsungBlue
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPreviewFile = null }) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SamsungSurface,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
private fun FileItemCard(
    file: UnclassifiedFile,
    onToggleSelect: () -> Unit,
    onPreviewClick: () -> Unit
) {
    OneUiCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreviewClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            IconButton(
                onClick = onToggleSelect,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (file.isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "اختيار الملف",
                    tint = if (file.isSelected) SamsungBlue else TextMuted
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // File Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = file.path,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(SamsungSurfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = file.extension.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamsungBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = FormatUtils.formatDate(file.lastModified),
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Size Tag
            Text(
                text = FormatUtils.formatBytes(file.sizeBytes),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }
    }
}
