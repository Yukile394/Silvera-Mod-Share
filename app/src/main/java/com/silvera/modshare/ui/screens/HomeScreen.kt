package com.silvera.modshare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silvera.modshare.scan.ScanReport
import com.silvera.modshare.ui.theme.RiskCritical
import com.silvera.modshare.ui.theme.SilveraAccent
import com.silvera.modshare.ui.theme.SilveraPurple

@Composable
fun HomeScreen(
    report: ScanReport?,
    lastScanTime: Long?,
    isScanning: Boolean,
    onStartScan: () -> Unit
) {
    val threatCount = report?.threats?.size ?: 0
    val isSafe = report == null || threatCount == 0

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(SilveraPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = SilveraPurple)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Settings, contentDescription = "Ayarlar")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Text("SİSTEM ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                if (isSafe) "GÜVENDE" else "TEHDİT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSafe) SilveraAccent else RiskCritical
            )
        }
        Text(
            lastScanTime?.let { "Son tarama: ${timeAgo(it)}" } ?: "Henüz tarama yapılmadı",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(140.dp).clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    (if (isSafe) SilveraAccent else RiskCritical).copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF7CC576), Color(0xFF5A8A3A))))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .align(Alignment.TopCenter)
                                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                                .background(Color(0xFF6FBF5A))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSafe) SilveraAccent else RiskCritical),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isSafe) Icons.Filled.Check else Icons.Filled.PriorityHigh,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Risk Durumu", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (isSafe) "GÜVENLİ" else "TEHDİT BULUNDU",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSafe) SilveraAccent else RiskCritical
                )
                Text(
                    if (isSafe) "Tehdit bulunamadı" else "$threatCount tehdit tespit edildi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(Icons.Filled.Folder, "${report?.scannedFileCount ?: 0}", "Dosyalar", Modifier.weight(1f))
            StatCard(Icons.Filled.Widgets, "${report?.scannedModCount ?: 0}", "Modlar", Modifier.weight(1f))
            StatCard(Icons.Filled.GpsFixed, "$threatCount", "Tehdit", Modifier.weight(1f))
            StatCard(Icons.Filled.VerifiedUser, "Aktif", "Koruma", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(SilveraPurple, Color(0xFF4C7EF0))))
                .clickable(enabled = !isScanning, onClick = onStartScan)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isScanning) "TARANIYOR..." else "TARAMA BAŞLAT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Cihazını şimdi tara",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("HIZLI ERİŞİM", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAccessCard(Icons.Filled.Folder, "Dosya Tarayıcı", "Depolamayı tara", Modifier.weight(1f))
            QuickAccessCard(Icons.Filled.Widgets, "Mod Tarayıcı", "Modları analiz et", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAccessCard(Icons.Filled.Shield, "Gerçek Zamanlı Koruma", "Arka planda koruma", Modifier.weight(1f))
            QuickAccessCard(Icons.Filled.Description, "Tarama Geçmişi", "Geçmiş raporları gör", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = SilveraPurple, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickAccessCard(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = SilveraPurple)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun timeAgo(timestamp: Long): String {
    val diffMinutes = (System.currentTimeMillis() - timestamp) / 60000
    return when {
        diffMinutes < 1 -> "az önce"
        diffMinutes < 60 -> "$diffMinutes dakika önce"
        diffMinutes < 1440 -> "${diffMinutes / 60} saat önce"
        else -> "${diffMinutes / 1440} gün önce"
    }
}
