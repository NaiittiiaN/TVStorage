package com.tvstorage.app.ui.screens.settings

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tvstorage.app.data.database.TVStorageDatabase
import com.tvstorage.app.data.repository.TelevisionRepository
import com.tvstorage.app.utils.ThemeStore
import com.tvstorage.app.utils.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val themeStore: ThemeStore,
    private val repository: TelevisionRepository,
    private val updateManager: UpdateManager
) : AndroidViewModel(application) {

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _showWhatsNew = MutableStateFlow(false)
    val showWhatsNew: StateFlow<Boolean> = _showWhatsNew

    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates

    val isDarkTheme: StateFlow<Boolean?> = themeStore.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val webPort: StateFlow<Int> = themeStore.webPort
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4848)

    val areAllPaused: StateFlow<Boolean> = repository.getAllActive().map { list ->
        list.isNotEmpty() && list.all { it.isPaused }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingUpdates.value = true
            val currentVersion = "1.2.6"
            val release = updateManager.checkForUpdates(currentVersion)
            if (release != null) {
                val apkAsset = release.assets.find { it.name.endsWith(".apk", ignoreCase = true) }
                if (apkAsset != null) {
                    _message.value = "Найдено обновление ${release.tag_name}. Скачиваю..."
                    updateManager.downloadAndInstall(apkAsset.browser_download_url)
                } else {
                    _message.value = "Обновление найдено, но APK не прикреплен"
                }
            } else {
                _message.value = "У вас последняя версия"
            }
            _isCheckingUpdates.value = false
        }
    }

    fun setAllPaused(isPaused: Boolean) {
        viewModelScope.launch {
            repository.setAllPaused(isPaused)
            _message.value = if (isPaused) "Все таймеры на паузе" else "Все таймеры запущены"
        }
    }

    fun setDarkTheme(isDark: Boolean?) {
        viewModelScope.launch { themeStore.setDarkTheme(isDark) }
    }

    fun setWebPort(port: Int) {
        viewModelScope.launch {
            themeStore.setWebPort(port)
            _message.value = "Порт изменен на $port. Перезагрузите приложение."
        }
    }

    fun exportDatabaseDirectly() {
        viewModelScope.launch {
            try {
                val dbFile = getApplication<Application>().getDatabasePath("tvstorage_database")
                if (!dbFile.exists()) {
                    _message.value = "База данных пуста"
                    return@launch
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                val fileName = "TVStorage_Backup_$timestamp.db"

                withContext(Dispatchers.IO) {
                    val resolver = getApplication<Application>().contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                    }

                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    } else {
                        // Для старых версий или спец. прошивок
                        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        Uri.fromFile(java.io.File(dir, fileName))
                    }

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { output ->
                            FileInputStream(dbFile).use { input -> input.copyTo(output) }
                        }
                        _message.value = "Бэкап сохранен в Загрузки: $fileName"
                    } else {
                        throw Exception("Ошибка доступа к папке Загрузки")
                    }
                }
            } catch (e: Exception) {
                _message.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            try {
                val dbFile = getApplication<Application>().getDatabasePath("tvstorage_database")
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(dbFile).use { output -> input.copyTo(output) }
                    }
                }
                _message.value = "База восстановлена! ПЕРЕЗАПУСТИТЕ ПРИЛОЖЕНИЕ!"
            } catch (e: Exception) {
                _message.value = "Ошибка загрузки: ${e.message}"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            TVStorageDatabase.getDatabase(getApplication()).clearAllTables()
            _message.value = "Все данные удалены"
        }
    }

    fun showDeleteConfirmation() { _showDeleteConfirmation.value = true }
    fun hideDeleteConfirmation() { _showDeleteConfirmation.value = false }
    fun clearMessage() { _message.value = null }
    fun showWhatsNew() { _showWhatsNew.value = true }
    fun hideWhatsNew() { _showWhatsNew.value = false }
}
