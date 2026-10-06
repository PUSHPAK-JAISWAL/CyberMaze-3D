package com.example.update

object VersionUtil {

    /**
     * Parses semantic version strings like "v1.2.3", "1.2.3", or "1.4.0" into [major, minor, patch].
     */
    fun parseVersion(versionStr: String): IntArray {
        val clean = versionStr.trim().removePrefix("v").removePrefix("V")
        val tokens = clean.split(".", "-").mapNotNull { it.toIntOrNull() }
        val major = tokens.getOrElse(0) { 1 }
        val minor = tokens.getOrElse(1) { 0 }
        val patch = tokens.getOrElse(2) { 0 }
        return intArrayOf(major, minor, patch)
    }

    /**
     * Compares two semantic version strings.
     * Returns true if remoteVersion is strictly newer than currentVersion.
     */
    fun isUpdateAvailable(currentVersion: String, remoteVersion: String): Boolean {
        val current = parseVersion(currentVersion)
        val remote = parseVersion(remoteVersion)

        for (i in 0 until 3) {
            if (remote[i] > current[i]) return true
            if (remote[i] < current[i]) return false
        }
        return false
    }

    fun formatVersion(raw: String): String {
        val parsed = parseVersion(raw)
        return "v${parsed[0]}.${parsed[1]}.${parsed[2]}"
    }
}
