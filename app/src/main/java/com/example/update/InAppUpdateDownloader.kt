package com.example.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class InAppUpdateDownloader(private val context: Context) {

    suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (progress: Float, downloadedMb: Float, totalMb: Float, speedMbs: Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val targetFile = File(updatesDir, "CyberMaze-3D-update.apk")
            if (targetFile.exists()) {
                targetFile.delete()
            }

            var currentUrl = downloadUrl
            var redirects = 0
            while (redirects < 5) {
                val urlObj = URL(currentUrl)
                connection = (urlObj.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "CyberMaze-3D-UpdateDownloader")
                }

                val status = connection.responseCode
                if (status in 300..399) {
                    val newLoc = connection.getHeaderField("Location") ?: break
                    connection.disconnect()
                    currentUrl = newLoc
                    redirects++
                } else {
                    break
                }
            }

            val conn = connection ?: throw RuntimeException("Failed to establish HTTP connection")
            if (conn.responseCode !in 200..299) {
                throw RuntimeException("HTTP download error ${conn.responseCode}")
            }

            val totalBytes = conn.contentLength.toLong()
            val totalMb = if (totalBytes > 0) totalBytes / (1024f * 1024f) else 15f

            var bytesReadTotal = 0L
            val startTime = System.currentTimeMillis()
            var lastSampleTime = startTime
            var lastSampleBytes = 0L
            var currentSpeedMbs = 0f

            conn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var count: Int
                    while (input.read(buffer).also { count = it } != -1) {
                        if (!coroutineContext.isActive) {
                            targetFile.delete()
                            throw RuntimeException("Download cancelled by user")
                        }

                        output.write(buffer, 0, count)
                        bytesReadTotal += count

                        val now = System.currentTimeMillis()
                        if (now - lastSampleTime >= 250) {
                            val timeDeltaSec = (now - lastSampleTime) / 1000f
                            val bytesDelta = bytesReadTotal - lastSampleBytes
                            if (timeDeltaSec > 0) {
                                currentSpeedMbs = (bytesDelta / (1024f * 1024f)) / timeDeltaSec
                            }
                            lastSampleTime = now
                            lastSampleBytes = bytesReadTotal

                            val progress = if (totalBytes > 0) (bytesReadTotal.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0.5f
                            val downloadedMb = bytesReadTotal / (1024f * 1024f)
                            onProgress(progress, downloadedMb, totalMb, currentSpeedMbs)
                        }
                    }
                }
            }

            // Final progress 100%
            onProgress(1.0f, totalMb, totalMb, currentSpeedMbs)
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
