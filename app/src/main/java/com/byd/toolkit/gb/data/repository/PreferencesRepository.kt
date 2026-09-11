package com.byd.toolkit.gb.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.byd.toolkit.gb.data.model.DrivingProfile
import com.byd.toolkit.gb.data.model.KeyConfig
import com.byd.toolkit.gb.data.model.LaunchSequenceConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("byd_toolkit_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_MASTER_ENABLED = "master_enabled"
        private const val KEY_KEY_REMAP_ENABLED = "key_remap_enabled"
        private const val KEY_KEY_HOOKS_PAUSED = "key_hooks_paused"
        private const val KEY_SELF_HEALING_ENABLED = "self_healing_enabled"
        private const val KEY_CONFIGS_JSON = "key_configs_json"
        private const val KEY_LAUNCH_SEQUENCE_JSON = "launch_sequence_json"
        private const val KEY_ACTIVE_PROFILE = "active_profile"
        private const val KEY_APP_LANGUAGE = "app_language"
    }

    var appLanguage: com.byd.toolkit.gb.data.model.AppLanguage
        get() {
            val code = prefs.getString(KEY_APP_LANGUAGE, "vi") ?: "vi"
            return com.byd.toolkit.gb.data.model.AppLanguage.fromCode(code)
        }
        set(value) = prefs.edit().putString(KEY_APP_LANGUAGE, value.code).apply()

    var isMasterServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_MASTER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MASTER_ENABLED, value).apply()

    var isKeyRemapServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_KEY_REMAP_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_KEY_REMAP_ENABLED, value).apply()

    var isKeyHooksPaused: Boolean
        get() = prefs.getBoolean(KEY_KEY_HOOKS_PAUSED, false)
        set(value) = prefs.edit().putBoolean(KEY_KEY_HOOKS_PAUSED, value).apply()

    var isSelfHealingEnabled: Boolean
        get() = prefs.getBoolean(KEY_SELF_HEALING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SELF_HEALING_ENABLED, value).apply()

    var activeProfile: DrivingProfile
        get() {
            val name = prefs.getString(KEY_ACTIVE_PROFILE, DrivingProfile.DAILY.name)
            return try {
                DrivingProfile.valueOf(name ?: DrivingProfile.DAILY.name)
            } catch (e: Exception) {
                DrivingProfile.DAILY
            }
        }
        set(value) = prefs.edit().putString(KEY_ACTIVE_PROFILE, value.name).apply()

    fun getKeyConfigs(): List<KeyConfig> {
        val json = prefs.getString(KEY_CONFIGS_JSON, null) ?: return KeyConfig.defaultStockKeys()
        return try {
            val type = object : TypeToken<List<KeyConfig>>() {}.type
            gson.fromJson<List<KeyConfig>>(json, type) ?: KeyConfig.defaultStockKeys()
        } catch (e: Exception) {
            KeyConfig.defaultStockKeys()
        }
    }

    fun saveKeyConfigs(configs: List<KeyConfig>) {
        val json = gson.toJson(configs)
        prefs.edit().putString(KEY_CONFIGS_JSON, json).apply()
    }

    fun updateKeyConfig(config: KeyConfig) {
        val list = getKeyConfigs().toMutableList()
        val index = list.indexOfFirst { it.keyCode == config.keyCode }
        if (index != -1) {
            list[index] = config
        } else {
            list.add(config)
        }
        saveKeyConfigs(list)
    }

    fun resetSingleKeyToStock(keyCode: Int) {
        val defaults = KeyConfig.defaultStockKeys()
        val defaultForKey = defaults.firstOrNull { it.keyCode == keyCode }
            ?: KeyConfig(keyCode, "Key $keyCode", "Phím nguyên bản")
        updateKeyConfig(defaultForKey)
    }

    fun resetAllKeysToStock() {
        saveKeyConfigs(KeyConfig.defaultStockKeys())
        isKeyHooksPaused = false
    }

    fun getLaunchSequenceConfig(): LaunchSequenceConfig {
        val json = prefs.getString(KEY_LAUNCH_SEQUENCE_JSON, null)
            ?: return LaunchSequenceConfig()
        return try {
            gson.fromJson(json, LaunchSequenceConfig::class.java) ?: LaunchSequenceConfig()
        } catch (e: Exception) {
            LaunchSequenceConfig()
        }
    }

    fun saveLaunchSequenceConfig(config: LaunchSequenceConfig) {
        val json = gson.toJson(config)
        prefs.edit().putString(KEY_LAUNCH_SEQUENCE_JSON, json).apply()
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
