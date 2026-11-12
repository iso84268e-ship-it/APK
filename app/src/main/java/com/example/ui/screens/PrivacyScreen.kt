package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.model.PrivacyAppInfo
import com.example.ui.PrivacyFilter
import com.example.ui.TabCleanerViewModel
import com.example.ui.components.OneUiCard
import com.example.ui.theme.SamsungBlue
import com.example.ui.theme.SamsungBlueGlow
import com.example.ui.theme.SamsungSurface
import com.example.ui.theme.SamsungSurfaceVariant
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun PrivacyScreen(
    viewModel: TabCleanerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apps by viewModel.filteredPrivacyApps.collectAsState()
    val isLoading by viewModel.isLoadingPrivacy.collectAsState()
    val currentFilter by viewModel.privacyFilter.collectAsState()
    val activeCount by viewModel.activePrivacyCount.collectAsState()

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
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SamsungBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.privacy_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.privacy_subtitle),
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = { viewModel.loadPrivacyApps() },
                enabled = !isLoading,
                modifier = Modifier
                    .background(SamsungSurfaceVariant, CircleShape)
                    .testTag("refresh_privacy_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث سجل الخصوصية",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open Samsung One UI Privacy Dashboard Banner Button
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "لوحة تحكم الخصوصية الرسمية",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "عرض الخط الزمني الكامل (آخر 24 ساعة) لأذونات الكاميرا والصوت",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        val intent = viewModel.privacyMonitor.createOpenPrivacyDashboardIntent()
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SamsungBlue),
                    modifier = Modifier.testTag("open_privacy_dashboard_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح اللوحة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System Policy Notice Card explaining restricted access
        OneUiCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 850.dp),
            backgroundColor = SamsungSurfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SamsungBlueGlow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.privacy_security_notice_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.privacy_security_notice_desc),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 850.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == PrivacyFilter.ALL,
                onClick = { viewModel.setPrivacyFilter(PrivacyFilter.ALL) },
                label = { Text("الكل (${apps.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SamsungBlue,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = currentFilter == PrivacyFilter.CAMERA,
                onClick = { viewModel.setPrivacyFilter(PrivacyFilter.CAMERA) },
                label = { Text("الكاميرا", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SamsungBlue,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = currentFilter == PrivacyFilter.MIC,
                onClick = { viewModel.setPrivacyFilter(PrivacyFilter.MIC) },
                label = { Text("الميكروفون", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SamsungBlue,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = currentFilter == PrivacyFilter.ACTIVE_NOW,
                onClick = { viewModel.setPrivacyFilter(PrivacyFilter.ACTIVE_NOW) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(StatusGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نشط الآن ($activeCount)", fontSize = 12.sp)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusGreenContainer,
                    selectedLabelColor = StatusGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Apps List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 850.dp)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = SamsungBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "جاري قراءة أذونات الكاميرا والصوت…",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }

                apps.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد تطبيقات تطابق هذا الفلتر",
                            fontSize = 15.sp,
                            color = TextMuted
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(apps, key = { it.packageName }) { appItem ->
                            PrivacyAppCard(app = appItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyAppCard(app: PrivacyAppInfo) {
    OneUiCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SamsungSurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    val bitmap: ImageBitmap? = remember(app.packageName) {
                        try {
                            app.icon.toBitmap(96, 96).asImageBitmap()
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.appName,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SamsungBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SamsungBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App Names and Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.isSystemApp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "نظام",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (app.lastAccessTime != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "آخر تحديث: ${FormatUtils.formatDate(app.lastAccessTime)}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Permissions Badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (app.hasCameraPermission) {
                    val isLive = app.isCameraActiveNow
                    Box(
                        modifier = Modifier
                            .background(
                                if (isLive) StatusGreenContainer else SamsungSurfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "كاميرا",
                                tint = if (isLive) StatusGreen else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            if (isLive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "نشط",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreen
                                )
                            }
                        }
                    }
                }

                if (app.hasMicPermission) {
                    val isLive = app.isMicActiveNow
                    Box(
                        modifier = Modifier
                            .background(
                                if (isLive) StatusGreenContainer else SamsungSurfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "ميكروفون",
                                tint = if (isLive) StatusGreen else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            if (isLive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "نشط",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
