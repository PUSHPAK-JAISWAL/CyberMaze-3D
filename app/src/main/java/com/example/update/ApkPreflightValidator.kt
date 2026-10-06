package com.example.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.security.MessageDigest

data class ValidationResult(
    val isValid: Boolean,
    val packageName: String?,
    val isSignatureMatch: Boolean,
    val conflictReason: String? = null
)

class ApkPreflightValidator(private val context: Context) {

    fun validateDownloadedApk(apkFile: File): ValidationResult {
        val pm = context.packageManager

        @Suppress("DEPRECATION")
        val archiveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES)
        }

        if (archiveInfo == null) {
            return ValidationResult(
                isValid = false,
                packageName = null,
                isSignatureMatch = false,
                conflictReason = "Downloaded APK package file appears corrupted or unparseable."
            )
        }

        val downloadedPackageName = archiveInfo.packageName
        val currentPackageName = context.packageName

        if (downloadedPackageName != currentPackageName) {
            return ValidationResult(
                isValid = false,
                packageName = downloadedPackageName,
                isSignatureMatch = false,
                conflictReason = "Package identifier mismatch (Expected $currentPackageName, got $downloadedPackageName)."
            )
        }

        val downloadedSignatures = getCertificateHashes(archiveInfo)
        val installedSignatures = getInstalledCertificateHashes()

        val isSignatureMatch = if (installedSignatures.isNotEmpty() && downloadedSignatures.isNotEmpty()) {
            downloadedSignatures.any { installedSignatures.contains(it) }
        } else {
            true // Allow system installer to verify if hashes cannot be extracted
        }

        return if (isSignatureMatch) {
            ValidationResult(
                isValid = true,
                packageName = downloadedPackageName,
                isSignatureMatch = true
            )
        } else {
            ValidationResult(
                isValid = false,
                packageName = downloadedPackageName,
                isSignatureMatch = false,
                conflictReason = "Cryptographic signature conflict detected. The installed app was signed with a different certificate."
            )
        }
    }

    private fun getCertificateHashes(packageInfo: PackageInfo): List<String> {
        val hashes = mutableListOf<String>()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = packageInfo.signingInfo
                if (signingInfo != null) {
                    val sigs = if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                    sigs?.forEach { hashes.add(computeSha256(it.toByteArray())) }
                }
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures?.forEach { hashes.add(computeSha256(it.toByteArray())) }
            }
        } catch (_: Exception) {}
        return hashes
    }

    private fun getInstalledCertificateHashes(): List<String> {
        return try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }
            getCertificateHashes(info)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun computeSha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString(":") { "%02X".format(it) }
    }
}
