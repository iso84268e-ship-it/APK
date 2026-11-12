package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.TabCleanerViewModel
import com.example.ui.components.OneUiCard
import com.example.ui.components.RamCircleGauge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SamsungBlue
import com.example.ui.theme.SamsungBlueContainer
import com.example.ui.theme.SamsungBlueGlow
import com.example.ui.theme.SamsungSurface
import com.example.ui.theme.SamsungSurfaceVariant
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun RamScreen(
    viewModel: TabCleanerViewModel,
    modifier: Modifier = Modifier
) {
    val ramInfo by viewModel.ramInfo.collectAsState()
    val isCleaning by viewModel.isCleaningRam.collectAsState()
    val cleanMsg by viewModel.ramCleanResultMsg.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = SamsungBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.ram_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "جهاز Samsung Galaxy Tab A9 • تحديث تلقائي",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = { viewModel.refreshRam() },
                enabled = !isCleaning,
                modifier = Modifier
                    .background(SamsungSurfaceVariant, CircleShape)
                    .testTag("refresh_ram_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clean result feedback banner if available
        AnimatedVisibility(
            visible = cleanMsg != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            cleanMsg?.let { msg ->
                OneUiCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 850.dp)
                        .padding(bottom = 20.dp),
                    backgroundColor = StatusGreenContainer,
                    borderColor = StatusGreen.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = StatusGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Circular Gauge Card
        OneUiCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 850.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusBadge(pressure = ramInfo.pressureLevel)

                Spacer(modifier = Modifier.height(24.dp))

                RamCircleGauge(
                    percentage = ramInfo.usagePercentage,
                    pressure = ramInfo.pressureLevel,
                    isCleaning = isCleaning,
                    size = 230.dp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Prominent "تنظيف الرام" Action Button
                Button(
                    onClick = { viewModel.cleanRam() },
                    enabled = !isCleaning,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SamsungBlue,
                        contentColor = Color.White,
                        disabledContainerColor = SamsungBlueContainer
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(0.75f)
                        .testTag("clean_ram_button")
                ) {
                    if (isCleaning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.ram_cleaning),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.ram_clean_button),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Real Statistics Grid Card
        OneUiCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 850.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "تفاصيل استهلاك الذاكرة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(
                        title = stringResource(R.string.ram_total),
                        value = FormatUtils.formatGigabytesOnly(ramInfo.totalBytes),
                        subValue = "${ramInfo.totalBytes / (1024 * 1024)} MB",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatBox(
                        title = stringResource(R.string.ram_used),
                        value = FormatUtils.formatGigabytesOnly(ramInfo.usedBytes),
                        subValue = "${ramInfo.usedBytes / (1024 * 1024)} MB",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatBox(
                        title = stringResource(R.string.ram_available),
                        value = FormatUtils.formatGigabytesOnly(ramInfo.availableBytes),
                        subValue = "${ramInfo.availableBytes / (1024 * 1024)} MB",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Android System Security & Realistic Memory Policy Explanation
        OneUiCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 850.dp),
            backgroundColor = SamsungSurfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SamsungBlueGlow,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.ram_notice_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.ram_notice_desc),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SamsungSurfaceVariant, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
