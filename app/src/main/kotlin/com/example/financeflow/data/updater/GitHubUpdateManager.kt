package com.example.financeflow.data.updater

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class GitHubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val apkDownloadUrl: String?,
    val publishedAt: String
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val release: GitHubRelease) : UpdateState()
    data class UpToDate(val currentVersion: String) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class GitHubUpdateManager(private val context: Context) {

    val currentVersion: String = "1.0.0"
    var configuredRepo: String = "rare-vault/Finance0"

    suspend fun checkForUpdates(repo: String = configuredRepo): UpdateState = withContext(Dispatchers.IO) {
        try {
            val endpoint = "https://api.github.com/repos/$repo/releases/latest"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Finance0-App")
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val tagName = json.optString("tag_name", "").removePrefix("v")
                val releaseName = json.optString("name", "Actualización Finance0")
                val releaseNotes = json.optString("body", "Mejoras generales y optimizaciones de notificaciones.")
                val publishedAt = json.optString("published_at", "")

                var apkDownloadUrl: String? = null
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = if (asset.has("browser_download_url")) asset.getString("browser_download_url") else null
                            break
                        }
                    }
                }

                val release = GitHubRelease(
                    tagName = tagName,
                    name = releaseName,
                    body = releaseNotes,
                    apkDownloadUrl = apkDownloadUrl,
                    publishedAt = publishedAt
                )

                if (isNewerVersion(tagName, currentVersion)) {
                    UpdateState.UpdateAvailable(release)
                } else {
                    UpdateState.UpToDate(currentVersion)
                }
            } else if (responseCode == 404) {
                UpdateState.UpToDate(currentVersion)
            } else {
                UpdateState.Error("Respuesta de GitHub: HTTP $responseCode")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateState.Error("Error de conexión: ${e.localizedMessage ?: "No se pudo contactar con GitHub"}")
        }
    }

    suspend fun downloadAndInstallApk(
        downloadUrl: String,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", "Finance0-App")
                connectTimeout = 15000
                readTimeout = 30000
            }

            val fileLength = connection.contentLength
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val outputFile = File(cacheDir, "Finance0_update.apk")
            if (outputFile.exists()) outputFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var total: Long = 0
                    var count: Int

                    while (input.read(buffer).also { count = it } != -1) {
                        total += count
                        output.write(buffer, 0, count)
                        if (fileLength > 0) {
                            val progress = ((total * 100) / fileLength).toInt()
                            onProgress(progress)
                        }
                    }
                }
            }
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun promptInstall(apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isNewerVersion(remoteTag: String, localVersion: String): Boolean {
        val remoteClean = remoteTag.trim().removePrefix("v")
        val localClean = localVersion.trim().removePrefix("v")
        return remoteClean != localClean && remoteClean.isNotBlank()
    }
}
