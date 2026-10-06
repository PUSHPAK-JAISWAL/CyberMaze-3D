package com.example.update

import java.io.File

data class UpdateInfo(
    val tagName: String,
    val versionName: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkSize: Long,
    val publishedAt: String,
    val htmlUrl: String
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data class Downloading(
        val info: UpdateInfo,
        val progress: Float, // 0.0f to 1.0f
        val downloadedMb: Float,
        val totalMb: Float,
        val speedMbs: Float
    ) : UpdateState()
    data class ReadyToInstall(
        val info: UpdateInfo,
        val apkFile: File,
        val isSignatureValid: Boolean
    ) : UpdateState()
    data class Conflict(
        val info: UpdateInfo,
        val apkFile: File,
        val reason: String
    ) : UpdateState()
    data class UpToDate(val checkedVersion: String) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
