package com.silvera.modshare

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.silvera.modshare.scan.ScanEngine
import com.silvera.modshare.scan.ScanReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _report = MutableStateFlow<ScanReport?>(null)
    val report: StateFlow<ScanReport?> = _report

    private val _lastScanTime = MutableStateFlow<Long?>(null)
    val lastScanTime: StateFlow<Long?> = _lastScanTime

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError

    fun startScan() {
        // Guard against double-taps, but never let a crashed/stuck scan block future scans forever.
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _scanError.value = null
            try {
                val result = withContext(Dispatchers.IO) { ScanEngine.runFullScan(getApplication()) }
                _report.value = result
                _lastScanTime.value = result.timestamp
            } catch (e: Exception) {
                // Önceden burada bir hata "Taranıyor..." ekranında sonsuza kadar takılı kalmaya
                // sebep oluyordu (isScanning hiç false olmuyordu). Artık her durumda kapanıyor.
                _scanError.value = e.message ?: "Tarama sırasında bir hata oluştu"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearThreats() {
        val current = _report.value ?: return
        ScanEngine.clearThreats(current.threats)
        _report.value = current.copy(threats = emptyList())
    }
}
