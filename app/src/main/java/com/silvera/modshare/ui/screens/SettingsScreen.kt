package com.silvera.modshare.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silvera.modshare.BuildConfig
import com.silvera.modshare.auth.AuthManager
import com.silvera.modshare.ui.theme.SilveraError
import com.silvera.modshare.ui.theme.SilveraPurple

@Composable
fun SettingsScreen(onLoggedOut: () -> Unit) {
    val context = LocalContext.current
    var avatarUriString by remember { mutableStateOf(AuthManager.avatarUri(context)) }
    var displayName by remember { mutableStateOf(AuthManager.currentName(context)) }
    var editingName by remember { mutableStateOf(false) }

    val avatarBitmap = remember(avatarUriString) {
        avatarUriString?.let { uriStr ->
            runCatching { loadBitmap(context, Uri.parse(uriStr)) }.getOrNull()
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            AuthManager.setAvatarUri(context, uri.toString())
            avatarUriString = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Ayarlar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(SilveraPurple.copy(alpha = 0.2f))
                        .clickable { pickImage.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap.asImageBitmap(),
                            contentDescription = "Profil fotoğrafı",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = SilveraPurple, modifier = Modifier.size(48.dp))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SilveraPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Fotoğrafı değiştir", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (editingName) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        singleLine = true,
                        label = { Text("Görünen ad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        TextButton(onClick = {
                            AuthManager.setDisplayName(context, displayName.ifBlank { AuthManager.currentName(context) })
                            editingName = false
                        }) { Text("Kaydet") }
                        TextButton(onClick = { editingName = false }) { Text("Vazgeç") }
                    }
                } else {
                    Text(displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        AuthManager.currentEmail(context) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = { editingName = true }) { Text("Adı Düzenle") }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsRow(label = "Uygulama Sürümü", value = BuildConfig.VERSION_NAME)
                Divider()
                SettingsRow(label = "Giriş Yöntemi", value = if (AuthManager.currentProvider(context) == "google") "Google" else "E-posta")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SilveraError.copy(alpha = 0.15f))
                .clickable {
                    AuthManager.logout(context)
                    onLoggedOut()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = SilveraError)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ÇIKIŞ YAP", color = SilveraError, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun loadBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }
}

