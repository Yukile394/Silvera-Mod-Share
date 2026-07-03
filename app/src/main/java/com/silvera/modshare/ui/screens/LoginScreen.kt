package com.silvera.modshare.ui.screens

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.silvera.modshare.auth.AuthManager
import com.silvera.modshare.ui.components.AnimatedPcBackground
import com.silvera.modshare.ui.components.TriggerIcon
import com.silvera.modshare.ui.theme.SilveraError
import com.silvera.modshare.ui.theme.SilveraPurple

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val context = LocalContext.current
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val googlePicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (accountName != null) {
                AuthManager.loginWithGoogleAccount(context, accountName)
                onLoggedIn()
            } else {
                error = "Hesap seçilmedi"
            }
        }
    }

    val accountsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null, null)
            googlePicker.launch(intent)
        } else {
            error = "Google hesabı seçmek için izin gerekli"
        }
    }

    fun launchGoogle() {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val intent = AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null, null)
            googlePicker.launch(intent)
        } else {
            accountsPermission.launch(Manifest.permission.GET_ACCOUNTS)
        }
    }

    AnimatedPcBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TriggerIcon(size = 64.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("SİLVERA", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Hile Koruma Sistemi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        ModeTab("Giriş Yap", !isRegisterMode, Modifier.weight(1f)) { isRegisterMode = false; error = null }
                        ModeTab("Kayıt Ol", isRegisterMode, Modifier.weight(1f)) { isRegisterMode = true; error = null }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Şifre") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(error!!, color = SilveraError, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(SilveraPurple, Color(0xFF4C7EF0))))
                            .clickable {
                                error = null
                                val result = if (isRegisterMode) {
                                    AuthManager.register(context, email, password)
                                } else {
                                    AuthManager.login(context, email, password)
                                }
                                result.onSuccess { onLoggedIn() }
                                    .onFailure { error = it.message }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isRegisterMode) "KAYIT OL" else "GİRİŞ YAP",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Divider(modifier = Modifier.weight(1f))
                        Text(
                            "  veya  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { launchGoogle() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Google Hesabıyla Devam Et", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Bilgilerin yalnızca bu cihazda saklanır.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModeTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) SilveraPurple else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

