package com.byd.toolkit.gb

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.byd.toolkit.gb.data.model.AppLanguage
import com.byd.toolkit.gb.service.SteeringKeyAccessibilityService
import com.byd.toolkit.gb.ui.components.TopCockpitBar
import com.byd.toolkit.gb.ui.screens.*
import com.byd.toolkit.gb.ui.theme.BYDToolkitTheme
import com.byd.toolkit.gb.util.LocaleHelper

sealed class Screen {
    object Dashboard : Screen()
    object AutoLaunch : Screen()
    object SteeringRemap : Screen()
    object RevertToStock : Screen()
}

class MainActivity : ComponentActivity() {

    private var hasAccessibilityState by mutableStateOf(false)
    private var hasOverlayState by mutableStateOf(false)
    private var currentLanguageState by mutableStateOf(AppLanguage.VIETNAMESE)

    override fun attachBaseContext(newBase: Context) {
        val prefs = BYDApplication.instance.preferencesRepository
        val lang = prefs.appLanguage
        val localized = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(localized)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = BYDApplication.instance.preferencesRepository
        currentLanguageState = prefs.appLanguage

        refreshPermissionStatus()

        setContent {
            BYDToolkitTheme {
                val hasAccess = hasAccessibilityState
                val hasOver = hasOverlayState
                val isGatekeeperPassed = hasAccess && hasOver

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                var showKeyDetective by remember { mutableStateOf(false) }

                if (!isGatekeeperPassed) {
                    // BLOCKING PERMISSION GATEKEEPER SCREEN
                    PermissionGatekeeperScreen(
                        hasAccessibility = hasAccess,
                        hasOverlay = hasOver,
                        onGrantAccessibility = { openAccessibilitySettings() },
                        onGrantOverlay = { openOverlaySettings() },
                        onRecheckPermissions = {
                            refreshPermissionStatus()
                            if (hasAccessibilityState && hasOverlayState) {
                                Toast.makeText(this@MainActivity, "Đã đủ quyền! Chào mừng vào BYD Toolkit GB.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "Vẫn còn quyền chưa bật trên màn hình xe.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onContinue = {
                            refreshPermissionStatus()
                        },
                        currentLanguage = currentLanguageState,
                        onLanguageChange = { newLang ->
                            changeAppLanguage(newLang)
                        }
                    )
                } else {
                    // MAIN APPLICATION
                    Scaffold(
                        topBar = {
                            TopCockpitBar(
                                currentLanguage = currentLanguageState,
                                onLanguageChange = { newLang ->
                                    changeAppLanguage(newLang)
                                },
                                onEmergencyClick = {
                                    currentScreen = Screen.RevertToStock
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                is Screen.Dashboard -> {
                                    DashboardScreen(
                                        onNavigateToAutoLaunch = { currentScreen = Screen.AutoLaunch },
                                        onNavigateToSteeringRemap = { currentScreen = Screen.SteeringRemap },
                                        onNavigateToRevertStock = { currentScreen = Screen.RevertToStock }
                                    )
                                }
                                is Screen.AutoLaunch -> {
                                    AutoLaunchScreen(
                                        onBackClick = { currentScreen = Screen.Dashboard }
                                    )
                                }
                                is Screen.SteeringRemap -> {
                                    SteeringRemapScreen(
                                        onBackClick = { currentScreen = Screen.Dashboard },
                                        onOpenKeyDetective = { showKeyDetective = true }
                                    )
                                }
                                is Screen.RevertToStock -> {
                                    RevertToStockScreen(
                                        onBackClick = { currentScreen = Screen.Dashboard }
                                    )
                                }
                            }

                            if (showKeyDetective) {
                                KeyDetectiveDialog(
                                    onDismiss = { showKeyDetective = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionStatus()
    }

    private fun refreshPermissionStatus() {
        hasAccessibilityState = checkAccessibilityService(this)
        hasOverlayState = checkOverlayPermission(this)
    }

    private fun changeAppLanguage(language: AppLanguage) {
        val prefs = BYDApplication.instance.preferencesRepository
        prefs.appLanguage = language
        currentLanguageState = language
        LocaleHelper.setLocale(this, language)
        // Recreate activity to apply resource bundle changes smoothly
        recreate()
    }

    private fun checkAccessibilityService(context: Context): Boolean {
        if (SteeringKeyAccessibilityService.instance != null) {
            return true
        }
        val expectedComponentName = ComponentName(context, SteeringKeyAccessibilityService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }

    private fun checkOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở cài đặt tiếp cận trên màn hình xe", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openOverlaySettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở cài đặt cửa sổ nổi trên màn hình xe", Toast.LENGTH_SHORT).show()
        }
    }
}
