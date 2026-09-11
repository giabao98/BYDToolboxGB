package com.byd.toolkit.gb.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.byd.toolkit.gb.data.model.ConfigSnapshot
import com.byd.toolkit.gb.data.model.KeyConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class SnapshotRepository(
    context: Context,
    private val preferencesRepository: PreferencesRepository
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("byd_toolkit_snapshots", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SNAPSHOT_LIST_JSON = "snapshot_list_json"
        private const val MAX_SNAPSHOTS = 10
    }

    fun ensureBaselineSnapshot() {
        val list = getSnapshots()
        if (list.none { it.isStockBaseline }) {
            val baseline = ConfigSnapshot(
                id = "baseline_stock",
                timestamp = System.currentTimeMillis(),
                title = "Nguyên bản gốc của xe (Stock Baseline)",
                description = "Cấu hình xuất xưởng gốc của xe BYD trước khi gán bất kỳ phím nào.",
                isStockBaseline = true,
                keyConfigs = KeyConfig.defaultStockKeys(),
                launchSequence = preferencesRepository.getLaunchSequenceConfig()
            )
            saveSnapshots(listOf(baseline) + list)
        }
    }

    fun getSnapshots(): List<ConfigSnapshot> {
        val json = prefs.getString(KEY_SNAPSHOT_LIST_JSON, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ConfigSnapshot>>() {}.type
            gson.fromJson<List<ConfigSnapshot>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun createSnapshot(title: String, description: String): ConfigSnapshot {
        val currentKeys = preferencesRepository.getKeyConfigs()
        val currentSeq = preferencesRepository.getLaunchSequenceConfig()

        val snapshot = ConfigSnapshot(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            title = title,
            description = description,
            isStockBaseline = false,
            keyConfigs = currentKeys,
            launchSequence = currentSeq
        )

        val list = getSnapshots().toMutableList()
        list.add(0, snapshot)

        // Trim to MAX_SNAPSHOTS preserving baseline
        val trimmed = if (list.size > MAX_SNAPSHOTS) {
            val baseline = list.find { it.isStockBaseline }
            val nonBaseline = list.filter { !it.isStockBaseline }.take(MAX_SNAPSHOTS - 1)
            if (baseline != null) nonBaseline + baseline else nonBaseline
        } else {
            list
        }

        saveSnapshots(trimmed)
        return snapshot
    }

    fun rollbackToSnapshot(snapshotId: String): Boolean {
        val snapshot = getSnapshots().find { it.id == snapshotId } ?: return false
        preferencesRepository.saveKeyConfigs(snapshot.keyConfigs)
        preferencesRepository.saveLaunchSequenceConfig(snapshot.launchSequence)
        preferencesRepository.isKeyHooksPaused = false
        return true
    }

    fun restoreFullStockCarState() {
        preferencesRepository.resetAllKeysToStock()
        val baseline = getSnapshots().find { it.isStockBaseline }
        if (baseline != null) {
            preferencesRepository.saveLaunchSequenceConfig(baseline.launchSequence)
        }
        preferencesRepository.isKeyHooksPaused = false
    }

    private fun saveSnapshots(list: List<ConfigSnapshot>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_SNAPSHOT_LIST_JSON, json).apply()
    }
}
