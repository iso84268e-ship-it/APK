package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.TabDestination
import com.example.ui.TabCleanerViewModel
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.RamScreen
import com.example.ui.theme.SamsungBackground
import com.example.ui.theme.SamsungBlue
import com.example.ui.theme.SamsungSurface
import com.example.ui.theme.TabCleanerTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: TabCleanerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TabCleanerTheme {
                // Arabic RTL Interface for Samsung Galaxy Tab
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    TabCleanerApp(
                        viewModel = viewModel,
                        onRequestStoragePermission = { requestStoragePermission() }
                    )
                }
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun TabCleanerApp(
    viewModel: TabCleanerViewModel,
    onRequestStoragePermission: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()

    // Back button brings user back to RAM monitor if on another tab
    BackHandler(enabled = currentTab != TabDestination.RAM) {
        viewModel.selectTab(TabDestination.RAM)
    }

    // Permission launcher for storage access
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        viewModel.setStoragePermissionGranted(granted)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SamsungBackground)
    ) {
        val isTabletLandscape = maxWidth >= 700.dp

        if (isTabletLandscape) {
            // Samsung Galaxy Tab Landscape layout: Side Navigation Rail + Main Content Pane
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = SamsungSurface,
                    contentColor = TextPrimary,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(SamsungBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tab Cleaner",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Galaxy Tab A9",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(110.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    NavigationRailItem(
                        selected = currentTab == TabDestination.RAM,
                        onClick = { viewModel.selectTab(TabDestination.RAM) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == TabDestination.RAM) Icons.Filled.Memory else Icons.Outlined.Memory,
                                contentDescription = stringResource(R.string.nav_ram)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_ram), fontSize = 12.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = SamsungBlue,
                            indicatorColor = SamsungBlue
                        ),
                        modifier = Modifier.testTag("nav_rail_ram")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NavigationRailItem(
                        selected = currentTab == TabDestination.FILES,
                        onClick = { viewModel.selectTab(TabDestination.FILES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == TabDestination.FILES) Icons.Filled.InsertDriveFile else Icons.Outlined.InsertDriveFile,
                                contentDescription = stringResource(R.string.nav_files)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_files), fontSize = 12.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = SamsungBlue,
                            indicatorColor = SamsungBlue
                        ),
                        modifier = Modifier.testTag("nav_rail_files")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NavigationRailItem(
                        selected = currentTab == TabDestination.PRIVACY,
                        onClick = { viewModel.selectTab(TabDestination.PRIVACY) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == TabDestination.PRIVACY) Icons.Filled.Security else Icons.Outlined.Security,
                                contentDescription = stringResource(R.string.nav_privacy)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_privacy), fontSize = 12.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = SamsungBlue,
                            indicatorColor = SamsungBlue
                        ),
                        modifier = Modifier.testTag("nav_rail_privacy")
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }

                // Main Content Pane
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    ScreenContent(currentTab = currentTab, viewModel = viewModel)
                }
            }
        } else {
            // Portrait layout: Scaffold with bottom NavigationBar
            Scaffold(
                containerColor = SamsungBackground,
                bottomBar = {
                    NavigationBar(
                        containerColor = SamsungSurface,
                        contentColor = TextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NavigationBarItem(
                            selected = currentTab == TabDestination.RAM,
                            onClick = { viewModel.selectTab(TabDestination.RAM) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == TabDestination.RAM) Icons.Filled.Memory else Icons.Outlined.Memory,
                                    contentDescription = stringResource(R.string.nav_ram)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_ram), fontSize = 12.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = SamsungBlue,
                                indicatorColor = SamsungBlue
                            ),
                            modifier = Modifier.testTag("bottom_nav_ram")
                        )

                        NavigationBarItem(
                            selected = currentTab == TabDestination.FILES,
                            onClick = { viewModel.selectTab(TabDestination.FILES) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == TabDestination.FILES) Icons.Filled.InsertDriveFile else Icons.Outlined.InsertDriveFile,
                                    contentDescription = stringResource(R.string.nav_files)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_files), fontSize = 12.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = SamsungBlue,
                                indicatorColor = SamsungBlue
                            ),
                            modifier = Modifier.testTag("bottom_nav_files")
                        )

                        NavigationBarItem(
                            selected = currentTab == TabDestination.PRIVACY,
                            onClick = { viewModel.selectTab(TabDestination.PRIVACY) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == TabDestination.PRIVACY) Icons.Filled.Security else Icons.Outlined.Security,
                                    contentDescription = stringResource(R.string.nav_privacy)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_privacy), fontSize = 12.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = SamsungBlue,
                                indicatorColor = SamsungBlue
                            ),
                            modifier = Modifier.testTag("bottom_nav_privacy")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(currentTab = currentTab, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    currentTab: TabDestination,
    viewModel: TabCleanerViewModel
) {
    Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
        when (tab) {
            TabDestination.RAM -> RamScreen(viewModel = viewModel)
            TabDestination.FILES -> FilesScreen(viewModel = viewModel)
            TabDestination.PRIVACY -> PrivacyScreen(viewModel = viewModel)
        }
    }
}
