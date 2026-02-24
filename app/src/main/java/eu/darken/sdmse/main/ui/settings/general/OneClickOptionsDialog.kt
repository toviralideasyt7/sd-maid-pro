package eu.darken.sdmse.main.ui.settings.general

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.darken.sdmse.R
import eu.darken.sdmse.common.datastore.valueBlocking
import eu.darken.sdmse.databinding.GeneralOnetapToolsDialogBinding
import eu.darken.sdmse.main.core.GeneralSettings
import javax.inject.Inject

class OneClickOptionsDialog @Inject constructor(private val settings: GeneralSettings) {

    fun show(context: Context): AlertDialog = MaterialAlertDialogBuilder(context).apply {
        setTitle(R.string.dashboard_settings_oneclick_tools_title)
        setMessage(R.string.dashboard_settings_oneclick_tools_desc)

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
        setView(binding.root)

    }.show()
}
