package com.silvera.modshare.scan

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import java.io.File
import java.util.UUID

object ScanEngine {

    // Dosya adında/mod adında geçtiğinde belirli bir hile ismiyle eşleştirilenler.
    private val KNOWN_CHEAT_MODS = listOf(
        Triple("killaura", "KillAura Mod", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("kill aura", "KillAura Mod", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("aimbot", "Aimbot (Trigger)", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("trigger", "Aimbot (Trigger)", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("triggerbot", "Aimbot (Trigger)", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("elytratarget", "ElytraTarget", RiskLevel.YUKSEK) to ThreatIcon.DOUBLE_ARROW,
        Triple("reach", "Reach++", RiskLevel.YUKSEK) to ThreatIcon.CROSSHAIR,
        Triple("autoclicker", "AutoClicker", RiskLevel.ORTA) to ThreatIcon.TAP,
        Triple("auto clicker", "AutoClicker", RiskLevel.ORTA) to ThreatIcon.TAP,
        Triple("aura", "KillAura Mod", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("wurst", "Wurst Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("impact", "Impact Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("sigma", "Sigma Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("meteor", "Meteor Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("aristois", "Aristois Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("salhack", "SalHack Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("kamihack", "KamiHack Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("matrixhud", "Matrix Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("future client", "Future Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("futureclient", "Future Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("liquidbounce", "LiquidBounce Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("hackedclient", "Hacked Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("hacked client", "Hacked Client", RiskLevel.KRITIK) to ThreatIcon.CROSSHAIR,
        Triple("toolbox", "ToolBox (Bedrock Hile)", RiskLevel.YUKSEK) to ThreatIcon.CROSSHAIR,
        Triple("xray", "XRay Mod", RiskLevel.YUKSEK) to ThreatIcon.SEARCH,
        Triple("x-ray", "XRay Mod", RiskLevel.YUKSEK) to ThreatIcon.SEARCH,
        Triple("wallhack", "WallHack / ESP", RiskLevel.YUKSEK) to ThreatIcon.SEARCH,
        Triple("esp", "WallHack / ESP", RiskLevel.YUKSEK) to ThreatIcon.SEARCH,
        Triple("flyhack", "FlyHack Mod", RiskLevel.YUKSEK) to ThreatIcon.CROSSHAIR,
        Triple("fly hack", "FlyHack Mod", RiskLevel.YUKSEK) to ThreatIcon.CROSSHAIR,
        Triple("speedhack", "SpeedHack Mod", RiskLevel.YUKSEK) to ThreatIcon.CROSSHAIR,
        Triple("speed hack", "SpeedHack Mod", RiskLevel.YUKSEK) to ThreatIcon.CROSSHAIR,
        Triple("nofall", "NoFall Mod", RiskLevel.ORTA) to ThreatIcon.CROSSHAIR,
        Triple("freecam", "FreeCam Mod", RiskLevel.ORTA) to ThreatIcon.CROSSHAIR,
        Triple("nuker", "Nuker Mod", RiskLevel.YUKSEK) to ThreatIcon.CROSSHAIR,
        Triple("scaffold", "Scaffold Mod", RiskLevel.ORTA) to ThreatIcon.CROSSHAIR
    )

    // Herhangi bir dosya/klasör isminde geçmesi bile şüpheli sayılan genel anahtar kelimeler.
    private val CHEAT_KEYWORDS = listOf(
        "hile", "hileli", "hackclient", "hack client", "cheatclient", "cheat client",
        "cracked", "crack client", "modmenu", "mod menu", "injector", "bypass"
    )

    // Bilinen sanal makine/launcher klasörleri de dahil, taranacak klasörler.
    private val SCAN_DIRS = listOf(
        "Download", "Downloads", "Documents",
        "Mods", ".minecraft", ".minecraft/mods",
        "games/com.mojang",
        "Android/data", "Android/obb",
        "Telegram", "Telegram/Telegram Documents",
        "WhatsApp/Media/WhatsApp Documents",
        "PojavLauncher", "TLauncher"
    )

    suspend fun runFullScan(context: Context): ScanReport {
        val start = System.currentTimeMillis()
        val threats = mutableListOf<ThreatResult>()
        var scannedFiles = 0
        var scannedMods = 0

        val root = Environment.getExternalStorageDirectory()
        val visited = mutableSetOf<String>()

        // 1) Bilinen mod/hile klasörlerini derinlemesine tara.
        SCAN_DIRS.forEach { rel ->
            try {
                val dir = File(root, rel)
                if (dir.exists() && dir.isDirectory) {
                    scanDirectory(dir, visited).forEach { file ->
                        try {
                            scannedFiles++
                            val lower = file.name.lowercase()
                            if (lower.endsWith(".jar") || lower.endsWith(".zip") || lower.endsWith(".apk")) {
                                scannedMods++
                            }
                            matchThreat(file)?.let { threats.add(it) }
                        } catch (e: Exception) {
                            // Bozuk/erişilemeyen tek bir dosya artık taramanın tamamını durdurmuyor.
                        }
                    }
                }
            } catch (e: Exception) {
                // İzin geri alınmış veya klasöre erişilemiyor olabilir; taramaya devam et.
            }
        }

        // 2) Depolama kökündeki dosya/klasörleri de (listede olmayanlar dahil) sığ şekilde tara.
        try {
            root.listFiles()?.forEach { entry ->
                try {
                    if (entry.isFile) {
                        if (visited.add(entry.absolutePath)) {
                            scannedFiles++
                            val lower = entry.name.lowercase()
                            if (lower.endsWith(".jar") || lower.endsWith(".zip") || lower.endsWith(".apk")) {
                                scannedMods++
                            }
                            matchThreat(entry)?.let { threats.add(it) }
                        }
                    } else if (entry.isDirectory && SCAN_DIRS.none { entry.name.equals(File(root, it).name, ignoreCase = true) }) {
                        scanDirectory(entry, visited, depth = 0, maxDepth = 3).forEach { file ->
                            try {
                                scannedFiles++
                                val lower = file.name.lowercase()
                                if (lower.endsWith(".jar") || lower.endsWith(".zip") || lower.endsWith(".apk")) {
                                    scannedMods++
                                }
                                matchThreat(file)?.let { threats.add(it) }
                            } catch (e: Exception) { }
                        }
                    }
                } catch (e: Exception) { }
            }
        } catch (e: Exception) { }

        // 3) Cihaza kurulu uygulamalar arasında hile/araç isimleriyle eşleşenleri bul.
        runCatching { threats.addAll(scanInstalledApps(context)) }

        // 4) ZArchiver gibi bilinen araçların kurulu olup olmadığını kontrol et.
        runCatching { detectZArchiver(context)?.let { threats.add(it) } }

        // 5) Tarayıcı geçmişi (best-effort, modern tarayıcılarda genelde sonuç dönmez).
        runCatching { threats.addAll(scanBrowserHistory(context)) }

        val distinct = threats.distinctBy { it.name + it.path }
        return ScanReport(
            threats = distinct,
            scannedFileCount = scannedFiles,
            scannedModCount = scannedMods,
            durationMs = System.currentTimeMillis() - start,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun scanDirectory(dir: File, visited: MutableSet<String>, depth: Int = 0, maxDepth: Int = 8): List<File> {
        if (depth > maxDepth) return emptyList()
        val result = mutableListOf<File>()
        val children = runCatching { dir.listFiles() }.getOrNull() ?: return result
        for (child in children) {
            try {
                if (!visited.add(child.absolutePath)) continue
                if (child.isDirectory) result.addAll(scanDirectory(child, visited, depth + 1, maxDepth))
                else result.add(child)
            } catch (e: Exception) {
                // Tek bir alt klasör/kısayol sorunlu olsa bile tarama kesilmesin.
            }
        }
        return result
    }

    private fun matchThreat(file: File): ThreatResult? {
        val lower = file.name.lowercase()
        val normalized = lower.replace(" ", "").replace("_", "").replace("-", "")
        for ((triple, icon) in KNOWN_CHEAT_MODS) {
            val (keyword, displayName, risk) = triple
            val normalizedKeyword = keyword.replace(" ", "")
            if (lower.contains(keyword) || normalized.contains(normalizedKeyword)) {
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
            if (normalized.contains(kw.replace(" ", ""))) {
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

    private fun scanInstalledApps(context: Context): List<ThreatResult> {
        val results = mutableListOf<ThreatResult>()
        val pm = context.packageManager
        val allKeywords = (CHEAT_KEYWORDS + KNOWN_CHEAT_MODS.map { it.first.first })
            .map { it.replace(" ", "") }
            .distinct()

        val installed: List<ApplicationInfo> = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }

        for (app in installed) {
            try {
                val pkg = app.packageName.lowercase()
                val label = pm.getApplicationLabel(app).toString().lowercase()
                val pkgNormalized = pkg.replace(".", "").replace("_", "")
                val labelNormalized = label.replace(" ", "").replace("_", "")

                val hit = allKeywords.firstOrNull { kw ->
                    pkgNormalized.contains(kw) || labelNormalized.contains(kw)
                }
                if (hit != null) {
                    results.add(
                        ThreatResult(
                            id = UUID.randomUUID().toString(),
                            name = pm.getApplicationLabel(app).toString(),
                            typeLabel = "Uygulama",
                            path = app.packageName,
                            risk = RiskLevel.YUKSEK,
                            icon = ThreatIcon.CROSSHAIR
                        )
                    )
                }
            } catch (e: Exception) {
                // Tek bir uygulama bilgisi okunamasa bile taramaya devam et.
            }
        }
        return results
    }

    private fun detectZArchiver(context: Context): ThreatResult? {
        val packages = listOf(
            "com.zarchiver",
            "ru.zdevs.zarchiver",
            "ru.zdevs.zarchiverpro",
            "ru.zdevs.zarchiver.pro"
        )
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
