package eu.darken.sdmse.scheduler.core

import eu.darken.sdmse.R
import eu.darken.sdmse.common.ca.CaString
import eu.darken.sdmse.common.ca.caString
import eu.darken.sdmse.main.core.SDMTool
import kotlinx.parcelize.Parcelize

@Parcelize
data class SchedulerMaintenanceTask(
    val scheduleId: ScheduleId,
    val killAppsRequested: Boolean,
    val trimCachesRequested: Boolean,
    val vacuumAppsRequested: Boolean = false,
    val purgeSystemLogsRequested: Boolean = false,
) : SDMTool.Task {
    override val type: SDMTool.Type
        get() = SDMTool.Type.APPCONTROL

    @Parcelize
    data class Result(
        val killAppsRequested: Boolean,
        val trimCachesRequested: Boolean,
        val stoppedPackages: List<String>,
        val failedPackages: List<String>,
        val trimSucceeded: Boolean,
        val reclaimedMb: Long,
        val vacuumAppsRequested: Boolean = false,
        val vacuumSucceededPackages: List<String> = emptyList(),
        val vacuumFailedPackages: List<String> = emptyList(),
        val purgeSystemLogsRequested: Boolean = false,
        val purgeLogsSucceeded: Boolean = false,
        val purgedPaths: List<String> = emptyList(),
    ) : SDMTool.Task.Result {
        override val type: SDMTool.Type
            get() = SDMTool.Type.APPCONTROL

        override val primaryInfo: CaString
            get() = caString {
                val parts = mutableListOf<String>()

                if (killAppsRequested) {
                    parts += resources.getQuantityString(
                        R.plurals.scheduler_maintenance_result_killed_x_apps,
                        stoppedPackages.size,
                        stoppedPackages.size,
                    )
                }

                if (trimCachesRequested) {
                    parts += if (trimSucceeded) {
                        getString(R.string.scheduler_maintenance_result_trim_x_mb, reclaimedMb)
                    } else {
                        getString(R.string.scheduler_maintenance_result_trim_failed)
                    }
                }

                if (vacuumAppsRequested) {
                    parts += resources.getQuantityString(
                        R.plurals.scheduler_maintenance_result_optimized_x_apps,
                        vacuumSucceededPackages.size,
                        vacuumSucceededPackages.size,
                    )
                }

                if (purgeSystemLogsRequested) {
                    parts += if (purgeLogsSucceeded) {
                        getString(R.string.scheduler_maintenance_result_log_purge_success)
                    } else {
                        getString(R.string.scheduler_maintenance_result_log_purge_failed)
                    }
                }

                if (parts.isEmpty()) {
                    getString(eu.darken.sdmse.common.R.string.general_result_success_message)
                } else {
                    parts.joinToString(" ")
                }
            }

        override val secondaryInfo: CaString?
            get() {
                if (
                    stoppedPackages.isEmpty() &&
                    failedPackages.isEmpty() &&
                    vacuumSucceededPackages.isEmpty() &&
                    vacuumFailedPackages.isEmpty() &&
                    !purgeSystemLogsRequested
                ) return null

                return caString {
                    val details = mutableListOf<String>()

                    if (stoppedPackages.isNotEmpty()) {
                        val shown = stoppedPackages.take(8)
                        val summary = buildString {
                            append(shown.joinToString(", "))
                            val remaining = stoppedPackages.size - shown.size
                            if (remaining > 0) append(" (+$remaining)")
                        }
                        details += getString(R.string.scheduler_maintenance_stopped_apps_x, summary)
                    }

                    if (failedPackages.isNotEmpty()) {
                        details += resources.getQuantityString(
                            eu.darken.sdmse.common.R.plurals.result_x_failed,
                            failedPackages.size,
                            failedPackages.size,
                        )
                    }

                    if (vacuumSucceededPackages.isNotEmpty()) {
                        val shown = vacuumSucceededPackages.take(8)
                        val summary = buildString {
                            append(shown.joinToString(", "))
                            val remaining = vacuumSucceededPackages.size - shown.size
                            if (remaining > 0) append(" (+$remaining)")
                        }
                        details += getString(R.string.scheduler_maintenance_optimized_apps_x, summary)
                    }

                    if (vacuumFailedPackages.isNotEmpty()) {
                        details += getString(
                            R.string.scheduler_maintenance_result_optimization_failed_x,
                            vacuumFailedPackages.size,
                        )
                    }

                    if (purgeSystemLogsRequested) {
                        val paths = purgedPaths.joinToString(", ")
                        details += getString(R.string.scheduler_maintenance_purged_paths_x, paths)
                    }

                    details.joinToString("\n")
                }
            }
    }
}
