package com.silvera.modshare

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.silvera.modshare.db.ModDatabase
import com.silvera.modshare.db.ModEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ModViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ModDatabase.getInstance(application).modDao()

    val mods: StateFlow<List<ModEntity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    fun addMod(uri: Uri, name: String, description: String) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val context = getApplication<Application>()
                val resolver = context.contentResolver

                val originalName = queryFileName(uri) ?: "mod_${System.currentTimeMillis()}"
                val extension = originalName.substringAfterLast('.', "")
                val safeFileName = "${UUID.randomUUID()}${if (extension.isNotEmpty()) ".$extension" else ""}"

                val modsDir = File(context.filesDir, "mods").apply { mkdirs() }
                val destFile = File(modsDir, safeFileName)

                var size = 0L
                withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { input ->
                        destFile.outputStream().use { output ->
                            size = input.copyTo(output)
                        }
                    }
                }

                dao.insert(
                    ModEntity(
                        name = name,
                        description = description,
                        fileName = originalName,
                        filePath = destFile.absolutePath,
                        fileSizeBytes = size,
                        dateAdded = System.currentTimeMillis()
                    )
                )
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteMod(mod: ModEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching { File(mod.filePath).delete() }
            }
            dao.delete(mod)
        }
    }

    private fun queryFileName(uri: Uri): String? {
        val context = getApplication<Application>()
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
