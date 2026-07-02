package com.silvera.modshare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silvera.modshare.scan.RiskLevel
import com.silvera.modshare.scan.ScanReport
import com.silvera.modshare.scan.ThreatIcon
import com.silvera.modshare.scan.ThreatResult
import com.silvera.modshare.ui.theme.RiskCritical
import com.silvera.modshare.ui.theme.RiskHigh
import com.silvera.modshare.ui.theme.RiskMedium
import com.silvera.modshare.ui.theme.SilveraAccent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScanReportScreen(
    report: ScanReport,
    onBack: () -> Unit,
    onClearThreats: () -> Unit,
    onSaveReport: () -> Unit
) {
    val threatCount = report.threats.size
    val maxRisk = report.threats.minByOrNull { it.risk.ordinal }?.risk
    val riskFraction = when (maxRisk) {
        RiskLevel.KRITIK -> 1f
        RiskLevel.YUKSEK -> 0.75f
        RiskLevel.ORTA -> 0.5f
        RiskLevel.DUSUK -> 0.25f
        null -> 0f
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Geri") }
                Text("Tarama Raporu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { }) { Icon(Icons.Filled.Share, contentDescription = "Paylaş") }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (threatCount > 0) RiskCritical.copy(alpha = 0.15f) else SilveraAccent.copy(alpha = 0.15f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background((if (threatCount > 0) RiskCritical else SilveraAccent).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (threatCount > 0) Icons.Filled.GppMaybe else Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = if (threatCount > 0) RiskCritical else SilveraAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (threatCount > 0) "TEHDİT BULUNDU" else "TEHDİT BULUNAMADI",
                            fontWeight = FontWeight.Bold,
                            color = if (threatCount > 0) RiskCritical else SilveraAccent
                        )
                        Text(
                            if (threatCount > 0) "Riskli içerikler tespit edildi" else "Cihaz temiz",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$threatCount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Toplam Tehdit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("RİSK SEVİYESİ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(riskLabel(maxRisk), fontWeight = FontWeight.Bold, color = riskColor(maxRisk))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val trackWidth = maxWidth
                        val knobSize = 18.dp
                        val knobOffset = (trackWidth - knobSize) * riskFraction

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(SilveraAccent, RiskMedium, RiskHigh, RiskCritical)))
                        )

                        Box(
                            modifier = Modifier
                                .padding(start = knobOffset)
                                .size(knobSize)
                                .clip(CircleShape)
                                .background(Color.White)
                                .align(Alignment.CenterStart)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(riskColor(maxRisk))
                                    .fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Güvenli", style = MaterialTheme.typography.labelSmall, color = if (maxRisk == null) SilveraAccent else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (maxRisk == null) FontWeight.Bold else FontWeight.Normal)
                        Text("Düşük", style = MaterialTheme.typography.labelSmall, color = if (maxRisk == RiskLevel.DUSUK) SilveraAccent else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (maxRisk == RiskLevel.DUSUK) FontWeight.Bold else FontWeight.Normal)
                        Text("Orta", style = MaterialTheme.typography.labelSmall, color = if (maxRisk == RiskLevel.ORTA) RiskMedium else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (maxRisk == RiskLevel.ORTA) FontWeight.Bold else FontWeight.Normal)
                        Text("Yüksek", style = MaterialTheme.typography.labelSmall, color = if (maxRisk == RiskLevel.YUKSEK) RiskHigh else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (maxRisk == RiskLevel.YUKSEK) FontWeight.Bold else FontWeight.Normal)
                        Text("Kritik", style = MaterialTheme.typography.labelSmall, color = if (maxRisk == RiskLevel.KRITIK) RiskCritical else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (maxRisk == RiskLevel.KRITIK) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        if (report.threats.isNotEmpty()) {
            item { Text("TESPİT EDİLEN TEHDİTLER", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(report.threats, key = { it.id }) { threat -> ThreatCard(threat) }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("TARAMA BİLGİLERİ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("Tarama Tarihi", formatDate(report.timestamp))
                    InfoRow("Taranan Dosya", "${report.scannedFileCount}")
                    InfoRow("Taranan Mod", "${report.scannedModCount}")
                    InfoRow("Tarama Süresi", formatDuration(report.durationMs))
                    InfoRow("Tarama Türü", "Tam Tarama")
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (report.threats.isNotEmpty())
                            Brush.horizontalGradient(listOf(Color(0xFF8E5CF0), Color(0xFF4C7EF0)))
                        else
                            Brush.horizontalGradient(listOf(Color(0xFF3A3A4A), Color(0xFF3A3A4A)))
                    )
                    .then(
                        if (report.threats.isNotEmpty()) Modifier.clickable(onClick = onClearThreats) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TEHDİTLERİ TEMİZLE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onSaveReport,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RAPORU KAYDET", fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun ThreatCard(threat: ThreatResult) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(riskColor(threat.risk).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconFor(threat.icon), contentDescription = null, tint = riskColor(threat.risk))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(threat.name, fontWeight = FontWeight.SemiBold)
                Text(threat.typeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(threat.path, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(threat.risk.label, color = riskColor(threat.risk), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier.padding(horizontal = 1.dp).size(6.dp).clip(CircleShape)
                                .background(if (index < threat.risk.dotCount) riskColor(threat.risk) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun iconFor(icon: ThreatIcon): ImageVector = when (icon) {
    ThreatIcon.CROSSHAIR -> Icons.Filled.GpsFixed
    ThreatIcon.DOUBLE_ARROW -> Icons.Filled.CompareArrows
    ThreatIcon.TAP -> Icons.Filled.TouchApp
    ThreatIcon.SEARCH -> Icons.Filled.Search
    ThreatIcon.ARCHIVE -> Icons.Filled.Archive
}

@Composable
private fun riskColor(risk: RiskLevel?) = when (risk) {
    RiskLevel.KRITIK -> RiskCritical
    RiskLevel.YUKSEK -> RiskHigh
    RiskLevel.ORTA -> RiskMedium
    RiskLevel.DUSUK -> SilveraAccent
    null -> SilveraAccent
}

private fun riskLabel(risk: RiskLevel?) = risk?.label ?: "Güvenli"

private fun formatDate(millis: Long): String = SimpleDateFormat("d MMMM yyyy HH:mm", Locale("tr")).format(Date(millis))

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    return "${totalSeconds / 60} dk ${totalSeconds % 60} sn"
}
