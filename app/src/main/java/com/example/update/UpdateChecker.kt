package com.example.update

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UpdateChecker(
    private val repoOwner: String = "PUSHPAK-JAISWAL",
    private val repoName: String = "cybermaze-3d"
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun checkLatestRelease(): Result<UpdateState> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "CyberMaze-3D-Android-Client")
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.code == 404) {
                return@withContext Result.success(UpdateState.UpToDate(BuildConfig.VERSION_NAME))
            }

            if (!response.isSuccessful) {
                return@withContext Result.failure(RuntimeException("GitHub API error HTTP ${response.code}: $body"))
            }

            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "")
            val releaseNotes = json.optString("body", "Bug fixes and performance improvements.")
            val publishedAt = json.optString("published_at", "")
            val htmlUrl = json.optString("html_url", "https://github.com/$repoOwner/$repoName/releases")

            // Look for .apk in assets
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            var apkSize: Long = 0L

            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url")
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            if (apkUrl == null) {
                // No APK asset attached yet to latest release
                return@withContext Result.success(UpdateState.UpToDate(BuildConfig.VERSION_NAME))
            }

            val currentVer = BuildConfig.VERSION_NAME
            val isNewer = VersionUtil.isUpdateAvailable(currentVer, tagName)

            if (isNewer) {
                val info = UpdateInfo(
                    tagName = tagName,
                    versionName = VersionUtil.formatVersion(tagName),
                    releaseNotes = releaseNotes,
                    apkDownloadUrl = apkUrl,
                    apkSize = apkSize,
                    publishedAt = publishedAt,
                    htmlUrl = htmlUrl
                )
                Result.success(UpdateState.Available(info))
            } else {
                Result.success(UpdateState.UpToDate(currentVer))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
