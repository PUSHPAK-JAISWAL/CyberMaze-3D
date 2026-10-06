package com.example.update

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class UpdateManager(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private val checker = UpdateChecker(repoOwner = "PUSHPAK-JAISWAL", repoName = "cybermaze-3d")
    private val downloader = InAppUpdateDownloader(context)
    private val validator = ApkPreflightValidator(context)
    private val installer = UpdateInstaller(context)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var downloadJob: Job? = null

    fun checkForUpdates(silent: Boolean = false) {
        if (_updateState.value is UpdateState.Checking || _updateState.value is UpdateState.Downloading) return

        _updateState.value = UpdateState.Checking
        scope.launch(Dispatchers.IO) {
            val res = checker.checkLatestRelease()
            res.onSuccess { state ->
                if (silent && state is UpdateState.UpToDate) {
                    _updateState.value = UpdateState.Idle
                } else {
                    _updateState.value = state
                }
            }.onFailure { err ->
                if (!silent) {
                    _updateState.value = UpdateState.Error(err.localizedMessage ?: "Failed to query GitHub Releases API")
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }
        }
    }

    fun startDownload(info: UpdateInfo) {
        if (_updateState.value is UpdateState.Downloading) return

        downloadJob?.cancel()
        downloadJob = scope.launch {
            _updateState.value = UpdateState.Downloading(
                info = info,
                progress = 0f,
                downloadedMb = 0f,
                totalMb = if (info.apkSize > 0) info.apkSize / (1024f * 1024f) else 15f,
                speedMbs = 0f
            )

            val downloadResult = downloader.downloadApk(info.apkDownloadUrl) { prog, downMb, totMb, speed ->
                _updateState.value = UpdateState.Downloading(
                    info = info,
                    progress = prog,
                    downloadedMb = downMb,
                    totalMb = totMb,
                    speedMbs = speed
                )
            }

            downloadResult.onSuccess { file ->
                val validation = validator.validateDownloadedApk(file)
                if (validation.isValid) {
                    _updateState.value = UpdateState.ReadyToInstall(
                        info = info,
                        apkFile = file,
                        isSignatureValid = true
                    )
                    // Launch installer
                    installer.launchPackageInstaller(file)
                } else {
                    _updateState.value = UpdateState.Conflict(
                        info = info,
                        apkFile = file,
                        reason = validation.conflictReason ?: "Signature conflict detected."
                    )
                }
            }.onFailure { err ->
                _updateState.value = UpdateState.Error("Download failed: ${err.localizedMessage}")
            }
        }
    }

    fun retryInstall(file: File) {
        installer.launchPackageInstaller(file)
    }

    fun exportToDownloads(file: File): Result<File> {
        return installer.saveApkToDownloads(file)
    }

    fun dismissUpdate() {
        downloadJob?.cancel()
        _updateState.value = UpdateState.Idle
    }
}
