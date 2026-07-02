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

    fun startScan() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            val result = withContext(Dispatchers.IO) { ScanEngine.runFullScan(getApplication()) }
            _report.value = result
            _lastScanTime.value = result.timestamp
            _isScanning.value = false
        }
    }

    fun clearThreats() {
        val current = _report.value ?: return
        ScanEngine.clearThreats(current.threats)
        _report.value = current.copy(threats = emptyList())
    }
}
