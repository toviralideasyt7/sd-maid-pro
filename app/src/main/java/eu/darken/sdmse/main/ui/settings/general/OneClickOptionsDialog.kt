package eu.darken.sdmse.main.ui.settings.general

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.darken.sdmse.R
import eu.darken.sdmse.common.datastore.valueBlocking
import eu.darken.sdmse.databinding.GeneralOnetapToolsDialogBinding
import eu.darken.sdmse.main.core.GeneralSettings
import javax.inject.Inject

class OneClickOptionsDialog @Inject constructor(private val settings: GeneralSettings) {

    data class ManualActions(
        val onRunKillAppsNow: (() -> Unit)? = null,
        val onRunTrimCacheNow: (() -> Unit)? = null,
        val onRunVacuumNow: (() -> Unit)? = null,
        val onRunPurgeLogsNow: (() -> Unit)? = null,
        val onRunSelectedNow: (() -> Unit)? = null,
    )

    fun show(context: Context, manualActions: ManualActions = ManualActions()): AlertDialog {
        val binding = GeneralOnetapToolsDialogBinding.inflate(LayoutInflater.from(context)).apply {
            corpsefinderToggle.isChecked = settings.oneClickCorpseFinderEnabled.valueBlocking
            systemcleanerToggle.isChecked = settings.oneClickSystemCleanerEnabled.valueBlocking
            appcleanerToggle.isChecked = settings.oneClickAppCleanerEnabled.valueBlocking
            killappsToggle.isChecked = settings.oneClickKillAppsEnabled.valueBlocking
            cachetrimToggle.isChecked = settings.oneClickCacheTrimEnabled.valueBlocking
            vacuumToggle.isChecked = settings.oneClickVacuumAppsEnabled.valueBlocking
            purgelogsToggle.isChecked = settings.oneClickPurgeLogsEnabled.valueBlocking
            deduplicatorToggle.isChecked = settings.oneClickDeduplicatorEnabled.valueBlocking

            corpsefinderToggle.setOnCheckedChangeListener { _, isChecked ->
                settings.oneClickCorpseFinderEnabled.valueBlocking = isChecked
            }
            systemcleanerToggle.setOnCheckedChangeListener { _, isChecked ->
                settings.oneClickSystemCleanerEnabled.valueBlocking = isChecked
            }
            appcleanerToggle.setOnCheckedChangeListener { _, isChecked ->
                settings.oneClickAppCleanerEnabled.valueBlocking = isChecked
            }
            killappsToggle.setOnCheckedChangeListener { _, isChecked ->
                settings.oneClickKillAppsEnabled.valueBlocking = isChecked
            }
            cachetrimToggle.setOnCheckedChangeListener { _, isChecked ->
                settings.oneClickCacheTrimEnabled.valueBlocking = isChecked
            }
            vacuumToggle.setOnCheckedChangeListener { _, isChecked ->
                settings.oneClickVacuumAppsEnabled.valueBlocking = isChecked
            }
            purgelogsToggle.setOnCheckedChangeListener { _, isChecked ->
                settings.oneClickPurgeLogsEnabled.valueBlocking = isChecked
            }
            deduplicatorToggle.setOnCheckedChangeListener { _, isChecked ->
                settings.oneClickDeduplicatorEnabled.valueBlocking = isChecked
            }
        }

        val hasManualActions = manualActions.onRunKillAppsNow != null ||
            manualActions.onRunTrimCacheNow != null ||
            manualActions.onRunVacuumNow != null ||
            manualActions.onRunPurgeLogsNow != null ||
            manualActions.onRunSelectedNow != null

        val dialog = MaterialAlertDialogBuilder(context).apply {
        setTitle(R.string.dashboard_settings_oneclick_tools_title)
        setMessage(R.string.dashboard_settings_oneclick_tools_desc)
        setView(binding.root)
    }.create()

        binding.manualActionsLabel.isVisible = hasManualActions

        binding.manualKillappsAction.apply {
            isVisible = manualActions.onRunKillAppsNow != null
            setOnClickListener {
                dialog.dismiss()
                manualActions.onRunKillAppsNow?.invoke()
            }
        }
        binding.manualCachetrimAction.apply {
            isVisible = manualActions.onRunTrimCacheNow != null
            setOnClickListener {
                dialog.dismiss()
                manualActions.onRunTrimCacheNow?.invoke()
            }
        }
        binding.manualVacuumAction.apply {
            isVisible = manualActions.onRunVacuumNow != null
            setOnClickListener {
                dialog.dismiss()
                manualActions.onRunVacuumNow?.invoke()
            }
        }
        binding.manualPurgelogsAction.apply {
            isVisible = manualActions.onRunPurgeLogsNow != null
            setOnClickListener {
                dialog.dismiss()
                manualActions.onRunPurgeLogsNow?.invoke()
            }
        }
        binding.manualRunselectedAction.apply {
            isVisible = manualActions.onRunSelectedNow != null
            setOnClickListener {
                dialog.dismiss()
                manualActions.onRunSelectedNow?.invoke()
            }
        }

        dialog.show()
        return dialog
    }
}
