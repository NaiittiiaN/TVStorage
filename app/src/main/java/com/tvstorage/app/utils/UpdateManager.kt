package com.tvstorage.app.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val assets: List<GitHubAsset>
)

@Serializable
data class GitHubAsset(
    val browser_download_url: String,
    val name: String
)

class UpdateManager(private val context: Context) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            })
        }
    }

    private val repoUrl = "https://api.github.com/repos/NaiittiiaN/TVStorage/releases/latest"

    suspend fun checkForUpdates(currentVersion: String): GitHubRelease? {
        return try {
            val response = client.get(repoUrl)
            val release: GitHubRelease = response.body()
            
            // Сравниваем тег версии (например "v1.2.3") с текущей версией
            val latestVersion = release.tag_name.removePrefix("v")
            if (latestVersion != currentVersion) {
                // Ищем любой файл, заканчивающийся на .apk (без учета регистра)
                val hasApk = release.assets.any { it.name.endsWith(".apk", ignoreCase = true) }
                if (hasApk) release else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun downloadAndInstall(url: String) {
        try {
            val downloadUrl = if (url.startsWith("http")) url else "https://github.com$url"
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Обновление TV Storage")
                .setDescription("Загрузка новой версии...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "TV_Storage_Update.apk")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            
            Toast.makeText(context, "Загрузка началась. После завершения нажмите на уведомление для установки.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка при запуске загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
