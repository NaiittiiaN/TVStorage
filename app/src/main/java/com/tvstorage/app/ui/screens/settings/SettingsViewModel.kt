package com.tvstorage.app.ui.screens.settings

import android.app.Application
import android.net.Uri
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
            val currentVersion = "1.2.3"
            val release = updateManager.checkForUpdates(currentVersion)
            if (release != null) {
                // Ищем файл с расширением .apk без учета регистра
                val apkAsset = release.assets.find { it.name.endsWith(".apk", ignoreCase = true) }
                if (apkAsset != null) {
                    _message.value = "Найдено обновление ${release.tag_name}. Начинаю скачивание..."
                    updateManager.downloadAndInstall(apkAsset.browser_download_url)
                } else {
                    _message.value = "Обновление найдено, но установочный файл (.apk) не прикреплен к релизу"
                }
            } else {
                _message.value = "У вас установлена последняя версия"
            }
            _isCheckingUpdates.value = false
        }
    }

    fun setAllPaused(isPaused: Boolean) {
        viewModelScope.launch {
            repository.setAllPaused(isPaused)
            _message.value = if (isPaused) "Все таймеры остановлены" else "Все таймеры запущены"
        }
    }

    fun setDarkTheme(isDark: Boolean?) {
        viewModelScope.launch {
            themeStore.setDarkTheme(isDark)
        }
    }

    fun setWebPort(port: Int) {
        viewModelScope.launch {
            themeStore.setWebPort(port)
            _message.value = "Порт изменен на $port. Перезапустите сервер."
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val db = TVStorageDatabase.getDatabase(getApplication())
            db.clearAllTables()
            _message.value = "Все данные удалены"
        }
    }

    fun backupDatabase(uri: Uri) {
        viewModelScope.launch {
            try {
                val dbFile = getApplication<Application>().getDatabasePath("tvstorage_database")
                if (dbFile.exists()) {
                    withContext(Dispatchers.IO) {
                        getApplication<Application>().contentResolver.openOutputStream(uri)?.use { output ->
                            FileInputStream(dbFile).use { input ->
                                input.copyTo(output)
                            }
                        }
                    }
                    _message.value = "База данных успешно сохранена"
                } else {
                    _message.value = "Файл базы данных не найден"
                }
            } catch (e: Exception) {
                _message.value = "Ошибка при сохранении: ${e.message}"
            }
        }
    }

    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            try {
                val dbFile = getApplication<Application>().getDatabasePath("tvstorage_database")
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(dbFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                _message.value = "База данных восстановлена. Перезапустите приложение."
            } catch (e: Exception) {
                _message.value = "Ошибка при загрузке: ${e.message}"
            }
        }
    }

    fun showDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun hideDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun clearMessage() {
        _message.value = null
    }

    fun showWhatsNew() {
        _showWhatsNew.value = true
    }

    fun hideWhatsNew() {
        _showWhatsNew.value = false
    }
}
