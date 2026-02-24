package eu.darken.sdmse.scheduler.core

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.darken.sdmse.common.adb.AdbManager
import eu.darken.sdmse.common.adb.canUseAdbNow
import eu.darken.sdmse.common.debug.logging.Logging.Priority.INFO
import eu.darken.sdmse.common.debug.logging.Logging.Priority.WARN
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.root.RootManager
import eu.darken.sdmse.common.root.canUseRootNow
import eu.darken.sdmse.common.shell.ShellOps
import eu.darken.sdmse.common.shell.ipc.ShellOpsCmd
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class MaintenanceOps @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootManager: RootManager,
    private val adbManager: AdbManager,
    private val shellOps: ShellOps,
) {

    suspend fun execute(
        killAppsRequested: Boolean,
        trimCachesRequested: Boolean,
        vacuumAppsRequested: Boolean = false,
        purgeSystemLogsRequested: Boolean = false,
    ): Execution {
        if (!killAppsRequested && !trimCachesRequested && !vacuumAppsRequested && !purgeSystemLogsRequested) {
            return Execution()
        }

        val shellMode = when {
            rootManager.canUseRootNow() -> ShellOps.Mode.ROOT
            adbManager.canUseAdbNow() -> ShellOps.Mode.ADB
            else -> null
        }

        if (shellMode == null) {
            return Execution(
                killAppsRequested = killAppsRequested,
                trimCachesRequested = trimCachesRequested,
                vacuumAppsRequested = vacuumAppsRequested,
                purgeSystemLogsRequested = purgeSystemLogsRequested,
                error = IllegalStateException("Maintenance actions require Root or Shizuku (ADB) access."),
            )
        }

        val stoppedPackages = mutableListOf<String>()
        val failedPackages = mutableListOf<String>()
        val vacuumSuccess = mutableListOf<String>()
        val vacuumFailed = mutableListOf<String>()

        if (killAppsRequested) {
            val killTargets = getKillablePackages()
            log(TAG, INFO) { "Killing ${killTargets.size} third-party apps via $shellMode" }

            killTargets.forEach { pkgName ->
                val result = shellOps.execute(ShellOpsCmd("am force-stop $pkgName"), shellMode)
                if (result.isSuccess) {
                    stoppedPackages.add(pkgName)
                } else {
                    failedPackages.add(pkgName)
                    log(TAG, WARN) { "Failed to stop $pkgName: ${result.errors}" }
                }
            }
        }

        var trimSucceeded = false
        var reclaimedMb = 0L
        if (trimCachesRequested) {
            val beforeMb = getAvailableExternalStorageMb()
            val trimResult = shellOps.execute(ShellOpsCmd("pm trim-caches 999G"), shellMode)
            trimSucceeded = trimResult.isSuccess
            if (trimSucceeded) {
                val afterMb = getAvailableExternalStorageMb()
                reclaimedMb = max(0L, afterMb - beforeMb)
            } else {
                log(TAG, WARN) { "Cache trim failed: ${trimResult.errors}" }
            }
        }

        if (vacuumAppsRequested) {
            val vacuumTargets = getKillablePackages()
            log(TAG, INFO) { "Optimizing ${vacuumTargets.size} apps via speed-profile" }
            vacuumTargets.forEach { pkgName ->
                val result = shellOps.execute(
                    ShellOpsCmd("cmd package compile -m speed-profile -f $pkgName"),
                    shellMode,
                )
                if (result.isSuccess) {
                    vacuumSuccess.add(pkgName)
                } else {
                    vacuumFailed.add(pkgName)
                    log(TAG, WARN) { "Failed to optimize $pkgName: ${result.errors}" }
                }
            }
        }

        var purgeLogsSucceeded = false
        if (purgeSystemLogsRequested) {
            val purgeResult = shellOps.execute(
                ShellOpsCmd(
                    "rm -rf /data/tombstones/*",
                    "rm -rf /data/log/*",
                ),
                shellMode,
            )
            purgeLogsSucceeded = purgeResult.isSuccess
            if (!purgeLogsSucceeded) {
                log(TAG, WARN) { "System log purge failed: ${purgeResult.errors}" }
            }
        }

        return Execution(
            killAppsRequested = killAppsRequested,
            trimCachesRequested = trimCachesRequested,
            vacuumAppsRequested = vacuumAppsRequested,
            purgeSystemLogsRequested = purgeSystemLogsRequested,
            stoppedPackages = stoppedPackages,
            failedPackages = failedPackages,
            trimSucceeded = trimSucceeded,
            reclaimedMb = reclaimedMb,
            vacuumSucceededPackages = vacuumSuccess,
            vacuumFailedPackages = vacuumFailed,
            purgeLogsSucceeded = purgeLogsSucceeded,
        )
    }

    private fun getKillablePackages(): List<String> {
        val excluded = setOf(
            context.packageName,
            "moe.shizuku.privileged.api",
            "com.termux",
            "com.termux.api",
            "com.termux.boot",
        )

        val isSystemApp: (ApplicationInfo) -> Boolean = { appInfo ->
            val system = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val updatedSystem = appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            system || updatedSystem
        }

        return context.packageManager
            .getInstalledPackages(0)
            .asSequence()
            .mapNotNull { pkgInfo ->
                val appInfo = pkgInfo.applicationInfo ?: return@mapNotNull null
                if (isSystemApp(appInfo)) return@mapNotNull null

                val pkgName = pkgInfo.packageName
                if (pkgName in excluded) return@mapNotNull null
                if (!VALID_PACKAGE_PATTERN.matches(pkgName)) return@mapNotNull null

                pkgName
            }
            .distinct()
            .sortedBy { it.lowercase(Locale.ROOT) }
            .toList()
    }

    @Suppress("DEPRECATION")
    private fun getAvailableExternalStorageMb(): Long {
        val statFs = StatFs(Environment.getExternalStorageDirectory().path)
        return statFs.availableBytes / MB_IN_BYTES
    }

    data class Execution(
        val killAppsRequested: Boolean = false,
        val trimCachesRequested: Boolean = false,
        val vacuumAppsRequested: Boolean = false,
        val purgeSystemLogsRequested: Boolean = false,
        val stoppedPackages: List<String> = emptyList(),
        val failedPackages: List<String> = emptyList(),
        val trimSucceeded: Boolean = false,
        val reclaimedMb: Long = 0,
        val vacuumSucceededPackages: List<String> = emptyList(),
        val vacuumFailedPackages: List<String> = emptyList(),
        val purgeLogsSucceeded: Boolean = false,
        val error: Exception? = null,
    )

    companion object {
        private val TAG = logTag("Scheduler", "MaintenanceOps")
        private val VALID_PACKAGE_PATTERN = Regex("[A-Za-z0-9_.]+")
        private const val MB_IN_BYTES = 1024L * 1024L
    }
}
