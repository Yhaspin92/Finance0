package com.example.financeflow.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
    data class NeedsInstallPermission(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class GitHubUpdateManager(private val context: Context) {

    val currentVersion: String
        get() = try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.3"
        } catch (_: Exception) {
            "1.3"
        }

    private val prefs = context.getSharedPreferences("finance0_updater_prefs", Context.MODE_PRIVATE)

    var configuredRepo: String
        get() = prefs.getString("github_repo", "Yhaspin92/Finance0") ?: "Yhaspin92/Finance0"
        set(value) = prefs.edit().putString("github_repo", value.trim()).apply()

    var personalAccessToken: String
        get() = prefs.getString("github_token", "") ?: ""
        set(value) = prefs.edit().putString("github_token", value.trim()).apply()

    suspend fun checkForUpdates(
        repo: String = configuredRepo,
        token: String = personalAccessToken
    ): UpdateState = withContext(Dispatchers.IO) {
        try {
            val cleanRepo = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
            val endpoint = "https://api.github.com/repos/$cleanRepo/releases/latest"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Finance0-App")
                if (token.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
                connectTimeout = 12000
                readTimeout = 12000
            }

            val responseCode = connection.responseCode
            when (responseCode) {
                200 -> {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                    val releaseName = json.optString("name", "Actualización Finance0")
                    val releaseNotes = json.optString("body", "Mejoras generales del motor de notificaciones y finanzas.")
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
                }
                404 -> {
                    UpdateState.Error("No se encontraron releases en '$cleanRepo'. Verifica que el repositorio exista y tenga al menos una versión publicada.")
                }
                401, 403 -> {
                    UpdateState.Error("Acceso denegado o límite de peticiones de GitHub alcanzado. Si el repositorio es privado, introduce un Personal Access Token.")
                }
                else -> {
                    UpdateState.Error("Respuesta inesperada de GitHub: HTTP $responseCode")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateState.Error("Error de conexión: ${e.localizedMessage ?: "No se pudo contactar con GitHub"}")
        }
    }

    suspend fun downloadAndInstallApk(
        downloadUrl: String,
        token: String = personalAccessToken,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            var currentUrl = downloadUrl
            var redirectCount = 0
            val maxRedirects = 8
            var finalConnection: HttpURLConnection? = null

            // Seguir redirecciones HTTP (GitHub Releases redirige a AWS S3 con 302 Found)
            while (redirectCount < maxRedirects) {
                val url = URL(currentUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "Finance0-App")
                    if (token.isNotBlank() && currentUrl.contains("api.github.com")) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    instanceFollowRedirects = true
                    connectTimeout = 15000
                    readTimeout = 30000
                }

                val code = conn.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_SEE_OTHER ||
                    code == 307 || code == 308
                ) {
                    val location = conn.getHeaderField("Location")
                    if (!location.isNullOrBlank()) {
                        conn.disconnect()
                        currentUrl = location
                        redirectCount++
                        continue
                    }
                }

                finalConnection = conn
                break
            }

            if (finalConnection == null || finalConnection.responseCode != 200) {
                return@withContext null
            }

            val fileLength = finalConnection.contentLength
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val outputFile = File(cacheDir, "Finance0_update.apk")
            if (outputFile.exists()) outputFile.delete()

            finalConnection.inputStream.use { input ->
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

    fun promptInstall(apkFile: File): Boolean {
        // En Android 8.0+ verificar si se permite instalar aplicaciones desconocidas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return false
            }
        }

        return try {
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
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Comparador SemVer robusto (ej. "1.0.1" > "1.0.0")
     */
    fun isNewerVersion(remoteTag: String, localVersion: String): Boolean {
        val rClean = remoteTag.trim().removePrefix("v").replace("-.*".toRegex(), "")
        val lClean = localVersion.trim().removePrefix("v").replace("-.*".toRegex(), "")

        val rParts = rClean.split(".").mapNotNull { it.toIntOrNull() }
        val lParts = lClean.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(rParts.size, lParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val l = lParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
