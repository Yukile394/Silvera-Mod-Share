package com.silvera.modshare.scan

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.util.UUID

object ScanEngine {

    private val KNOWN_CHEAT_MODS = listOf(
        Triple("killaura", "KillAura Mod", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("aimbot", "Aimbot (Trigger)", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("trigger", "Aimbot (Trigger)", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("elytratarget", "ElytraTarget", RiskLevel.YUKSEK) to ThreatIcon.DOUBLE_ARROW,
        Triple("reach", "Reach++", RiskLevel.YUKSEK) to ThreatIcon.CROSSHAIR,
        Triple("autoclicker", "AutoClicker", RiskLevel.ORTA) to ThreatIcon.TAP,
        Triple("aura", "KillAura Mod", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("wurst", "Wurst Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("impact", "Impact Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("sigma", "Sigma Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("xray", "XRay Mod", RiskLevel.YUKSEK) to ThreatIcon.SEARCH
    )

    private val CHEAT_KEYWORDS = listOf("hile", "hackclient", "hileli", "cheatclient")

    private val SCAN_DIRS = listOf("Download", "Downloads", "Mods", "games/com.mojang", "Android/data")

    suspend fun runFullScan(context: Context): ScanReport {
        val start = System.currentTimeMillis()
        val threats = mutableListOf<ThreatResult>()
        var scannedFiles = 0
        var scannedMods = 0

        val root = Environment.getExternalStorageDirectory()
        val visited = mutableSetOf<String>()

        SCAN_DIRS.forEach { rel ->
            val dir = File(root, rel)
            if (dir.exists() && dir.isDirectory) {
                scanDirectory(dir, visited).forEach { file ->
                    scannedFiles++
                    val lower = file.name.lowercase()
                    if (lower.endsWith(".jar") || lower.endsWith(".zip") || lower.endsWith(".apk")) {
                        scannedMods++
                    }
                    matchThreat(file)?.let { threats.add(it) }
                }
            }
        }

        detectZArchiver(context)?.let { threats.add(it) }
        threats.addAll(scanBrowserHistory(context))

        val distinct = threats.distinctBy { it.name + it.path }
        return ScanReport(
            threats = distinct,
            scannedFileCount = scannedFiles,
            scannedModCount = scannedMods,
            durationMs = System.currentTimeMillis() - start,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun scanDirectory(dir: File, visited: MutableSet<String>, depth: Int = 0): List<File> {
        if (depth > 6) return emptyList()
        val result = mutableListOf<File>()
        val children = dir.listFiles() ?: return result
        for (child in children) {
            if (!visited.add(child.absolutePath)) continue
            if (child.isDirectory) result.addAll(scanDirectory(child, visited, depth + 1))
            else result.add(child)
        }
        return result
    }

    private fun matchThreat(file: File): ThreatResult? {
        val lower = file.name.lowercase()
        for ((triple, icon) in KNOWN_CHEAT_MODS) {
            val (keyword, displayName, risk) = triple
            if (lower.contains(keyword)) {
                val ext = file.extension.ifBlank { "jar" }
                return ThreatResult(
                    id = UUID.randomUUID().toString(),
                    name = displayName,
                    typeLabel = "Mod – .$ext",
                    path = file.absolutePath,
                    risk = risk,
                    icon = icon
                )
            }
        }
        for (kw in CHEAT_KEYWORDS) {
            if (lower.replace(" ", "").contains(kw)) {
                return ThreatResult(
                    id = UUID.randomUUID().toString(),
                    name = file.name,
                    typeLabel = "Dosya",
                    path = file.absolutePath,
                    risk = RiskLevel.ORTA,
                    icon = ThreatIcon.SEARCH
                )
            }
        }
        return null
    }

    private fun detectZArchiver(context: Context): ThreatResult? {
        val packages = listOf("com.zarchiver", "ru.zdevs.zarchiver")
        val pm = context.packageManager
        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return ThreatResult(
                    id = UUID.randomUUID().toString(),
                    name = "ZArchiver kurulu",
                    typeLabel = "Uygulama",
                    path = pkg,
                    risk = RiskLevel.DUSUK,
                    icon = ThreatIcon.ARCHIVE
                )
            } catch (e: Exception) { }
        }
        return null
    }

    private fun scanBrowserHistory(context: Context): List<ThreatResult> {
        val results = mutableListOf<ThreatResult>()
        val providerUris = listOf("content://com.android.browser/history", "content://browser/bookmarks")
        val allKeywords = CHEAT_KEYWORDS + KNOWN_CHEAT_MODS.map { it.first.first }

        for (uriStr in providerUris) {
            try {
                context.contentResolver.query(Uri.parse(uriStr), null, null, null, null)?.use { cursor ->
                    val titleIdx = cursor.getColumnIndex("title")
                    val urlIdx = cursor.getColumnIndex("url")
                    while (cursor.moveToNext()) {
                        val title = if (titleIdx >= 0) cursor.getString(titleIdx).orEmpty() else ""
                        val url = if (urlIdx >= 0) cursor.getString(urlIdx).orEmpty() else ""
                        val combined = (title + " " + url).lowercase()
                        val hit = allKeywords.firstOrNull { combined.contains(it) }
                        if (hit != null) {
                            results.add(
                                ThreatResult(
                                    id = UUID.randomUUID().toString(),
                                    name = title.ifBlank { url },
                                    typeLabel = "Arama Geçmişi",
                                    path = url,
                                    risk = RiskLevel.ORTA,
                                    icon = ThreatIcon.SEARCH
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) { }
        }
        return results
    }

    fun clearThreats(threats: List<ThreatResult>) {
        threats.forEach { threat ->
            if (threat.typeLabel.startsWith("Mod") || threat.typeLabel == "Dosya") {
                runCatching { File(threat.path).delete() }
            }
        }
    }
}
