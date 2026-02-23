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
    ) : SDMTool.Task.Result {
        override val type: SDMTool.Type
            get() = SDMTool.Type.APPCONTROL

        override val primaryInfo: CaString
            get() = caString {
                when {
                    killAppsRequested && trimCachesRequested -> {
                        if (trimSucceeded) {
                            getString(
                                R.string.scheduler_maintenance_result_kill_trim,
                                stoppedPackages.size,
                                reclaimedMb
                            )
                        } else {
                            val stopped = resources.getQuantityString(
                                R.plurals.scheduler_maintenance_result_killed_x_apps,
                                stoppedPackages.size,
                                stoppedPackages.size,
                            )
                            "$stopped ${getString(R.string.scheduler_maintenance_result_trim_failed)}"
                        }
                    }

                    killAppsRequested -> {
                        resources.getQuantityString(
                            R.plurals.scheduler_maintenance_result_killed_x_apps,
                            stoppedPackages.size,
                            stoppedPackages.size,
                        )
                    }

                    trimCachesRequested -> {
                        if (trimSucceeded) {
                            getString(R.string.scheduler_maintenance_result_trim_x_mb, reclaimedMb)
                        } else {
                            getString(R.string.scheduler_maintenance_result_trim_failed)
                        }
                    }

                    else -> getString(eu.darken.sdmse.common.R.string.general_result_success_message)
                }
            }

        override val secondaryInfo: CaString?
            get() {
                if (stoppedPackages.isEmpty() && failedPackages.isEmpty()) return null

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

                    details.joinToString("\n")
                }
            }
    }
}
