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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silvera.modshare.auth.AuthManager
import com.silvera.modshare.ui.components.AnimatedPcBackground
import com.silvera.modshare.ui.screens.HomeScreen
import com.silvera.modshare.ui.screens.LoginScreen
import com.silvera.modshare.ui.screens.ScanReportScreen
import com.silvera.modshare.ui.screens.SettingsScreen
import com.silvera.modshare.ui.screens.shareReport
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SilveraApp(viewModel: ScanViewModel) {
    val context = LocalContext.current
    var loggedIn by remember { mutableStateOf(AuthManager.isLoggedIn(context)) }

    if (!loggedIn) {
        LoginScreen(onLoggedIn = { loggedIn = true })
        return
    }

    var currentScreen by remember { mutableStateOf(SilveraScreen.HOME) }
    var menuExpanded by remember { mutableStateOf(false) }

    val isScanning by viewModel.isScanning.collectAsState()
    val report by viewModel.report.collectAsState()
    val lastScanTime by viewModel.lastScanTime.collectAsState()
    val scanError by viewModel.scanError.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("SİLVERA", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menü")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Raporu Paylaş") },
                                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                                enabled = report != null,
                                onClick = {
                                    menuExpanded = false
                                    report?.let { shareReport(context, it) }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sürüm ${BuildConfig.VERSION_NAME}") },
                                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                                enabled = false,
                                onClick = {}
                            )
                        }
                    }
                }
            )
        },
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
                    onClick = {
                        currentScreen = SilveraScreen.RAPOR
                        viewModel.startScan()
                    },
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
        AnimatedPcBackground(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                SilveraScreen.RAPOR -> {
                    if (isScanning || (report == null && scanError == null)) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cihaz taranıyor...")
                        }
                    } else if (scanError != null && report == null) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tarama başarısız oldu: $scanError", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.startScan() }) { Text("Tekrar Dene") }
                        }
                    } else {
                        ScanReportScreen(
                            report = report!!,
                            onBack = { currentScreen = SilveraScreen.HOME },
                            onClearThreats = { viewModel.clearThreats() },
                            onSaveReport = { report?.let { shareReport(context, it) } }
                        )
                    }
                }
                SilveraScreen.GECMIS -> {
                    if (report != null) {
                        ScanReportScreen(
                            report = report!!,
                            onBack = { currentScreen = SilveraScreen.HOME },
                            onClearThreats = { viewModel.clearThreats() },
                            onSaveReport = { report?.let { shareReport(context, it) } }
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
                    SettingsScreen(onLoggedOut = {
                        loggedIn = false
                        currentScreen = SilveraScreen.HOME
                    })
                }
                SilveraScreen.HOME -> {
                    HomeScreen(
                        report = report,
                        lastScanTime = lastScanTime,
                        isScanning = isScanning,
                        onStartScan = {
                            currentScreen = SilveraScreen.RAPOR
                            viewModel.startScan()
                        },
                        onOpenSettings = { currentScreen = SilveraScreen.AYARLAR }
                    )
                }
            }
        }
    }
}
