package com.silvera.modshare.scan

enum class RiskLevel(val label: String, val dotCount: Int) {
    KRITIK("Kritik", 4),
    YUKSEK("Yüksek", 3),
    ORTA("Orta", 2),
    DUSUK("Düşük", 1)
}

enum class ThreatIcon { CROSSHAIR, DOUBLE_ARROW, TAP, SEARCH, ARCHIVE }

data class ThreatResult(
    val id: String,
    val name: String,
    val typeLabel: String,
    val path: String,
    val risk: RiskLevel,
    val icon: ThreatIcon
)

data class ScanReport(
    val threats: List<ThreatResult>,
    val scannedFileCount: Int,
    val scannedModCount: Int,
    val durationMs: Long,
    val timestamp: Long
)
