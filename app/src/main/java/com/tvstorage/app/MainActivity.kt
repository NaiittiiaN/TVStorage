package com.tvstorage.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tvstorage.app.navigation.TVStorageNavHost
import com.tvstorage.app.ui.theme.TVStorageTheme
import com.tvstorage.app.utils.ThemeStore
import com.tvstorage.app.web.WebServerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.Intent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var themeStore: ThemeStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Запуск веб-сервера
        val webServiceIntent = Intent(this, WebServerService::class.java)
        startForegroundService(webServiceIntent)

        enableEdgeToEdge()
        setContent {
            val isDarkThemePref by themeStore.isDarkTheme.collectAsState(initial = null)
            val darkTheme = isDarkThemePref ?: isSystemInDarkTheme()

            TVStorageTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TVStorageNavHost()
                }
            }
        }
    }

    override fun onDestroy() {
        // Остановка веб-сервера при закрытии приложения
        val webServiceIntent = Intent(this, WebServerService::class.java)
        stopService(webServiceIntent)
        super.onDestroy()
    }
}
