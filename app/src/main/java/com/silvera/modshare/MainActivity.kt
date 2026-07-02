package com.silvera.modshare

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.silvera.modshare.ui.screens.HomeScreen
import com.silvera.modshare.ui.screens.ScanReportScreen
import com.silvera.modshare.ui.theme.SilveraModShareTheme

enum class SilveraScreen { HOME, RAPOR, GECMIS, AYARLAR }

class MainActivity : ComponentActivity() {

    private val viewModel: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            runCatching {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        .setData(Uri.parse("package:$packageName"))
                )
            }
        }

        setContent {
            SilveraModShareTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SilveraApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun SilveraApp(viewModel: ScanViewModel) {
    var currentScreen by remember { mutableStateOf(SilveraScreen.HOME) }

    val isScanning by viewModel.isScanning.collectAsState()
    val report by viewModel.report.collectAsState()
    val lastScanTime by viewModel.lastScanTime.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = currentScreen == SilveraScreen.HOME,
                    onClick = { currentScreen = SilveraScreen.HOME },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Ana Sayfa") }
                )
                NavigationBarItem(
                    selected = currentScreen == SilveraScreen.RAPOR,
                    onClick = { currentScreen = SilveraScreen.RAPOR; viewModel.startScan() },
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text("Tarama") }
                )
                NavigationBarItem(
                    selected = currentScreen == SilveraScreen.GECMIS,
                    onClick = { currentScreen = SilveraScreen.GECMIS },
                    icon = { Icon(Icons.Filled.Description, contentDescription = null) },
                    label = { Text("Raporlar") }
                )
                NavigationBarItem(
                    selected = currentScreen == SilveraScreen.AYARLAR,
                    onClick = { currentScreen = SilveraScreen.AYARLAR },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Ayarlar") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                SilveraScreen.RAPOR -> {
                    if (isScanning || report == null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cihaz taranıyor...")
                        }
                    } else {
                        ScanReportScreen(
                            report = report!!,
                            onBack = { currentScreen = SilveraScreen.HOME },
                            onClearThreats = { viewModel.clearThreats() },
                            onSaveReport = { }
                        )
                    }
                }
                SilveraScreen.GECMIS -> {
                    if (report != null) {
                        ScanReportScreen(
                            report = report!!,
                            onBack = { currentScreen = SilveraScreen.HOME },
                            onClearThreats = { viewModel.clearThreats() },
                            onSaveReport = { }
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) { Text("Henüz tarama yapılmadı") }
                    }
                }
                SilveraScreen.AYARLAR -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) { Text("Ayarlar yakında") }
                }
                SilveraScreen.HOME -> {
                    HomeScreen(
                        report = report,
                        lastScanTime = lastScanTime,
                        isScanning = isScanning,
                        onStartScan = { currentScreen = SilveraScreen.RAPOR; viewModel.startScan() }
                    )
                }
            }
        }
    }
}
