package com.byd.toolkit.gb

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
                var isGatekeeperBypassed by remember { mutableStateOf(false) }
                val isGatekeeperPassed = (hasAccess && hasOver) || isGatekeeperBypassed

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
                                Toast.makeText(this@MainActivity, "Đã đủ toàn bộ quyền! Chào mừng vào BYD Toolkit GB.", Toast.LENGTH_SHORT).show()
                            } else if (hasAccessibilityState) {
                                Toast.makeText(this@MainActivity, "Trợ Năng: ĐÃ BẬT ✓ | Cửa Sổ Nổi: CHƯA BẬT ✕ (Có thể bấm Vào Ứng Dụng)", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this@MainActivity, "Chưa nhận diện được quyền Trợ Năng. Hãy kiểm tra kết nối ADB.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onContinue = {
                            refreshPermissionStatus()
                            isGatekeeperBypassed = true
                            if (!hasOverlayState) {
                                Toast.makeText(this@MainActivity, "Đã vào ứng dụng! (Lưu ý: Thiếu Cửa Sổ Nổi sẽ không hiện bảng đếm ngược khi nổ máy)", Toast.LENGTH_LONG).show()
                            }
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
        // Ưu tiên 1: Service instance đang hoạt động
        if (SteeringKeyAccessibilityService.instance != null) {
            return true
        }

        // Ưu tiên 2: Kiểm tra qua AccessibilityManager hệ thống
        try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            if (enabledServices != null) {
                for (service in enabledServices) {
                    val serviceName = service.resolveInfo?.serviceInfo?.name ?: ""
                    if (serviceName.contains("SteeringKeyAccessibilityService")) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // Bỏ qua ngoại lệ hệ thống
        }

        // Ưu tiên 3: Kiểm tra Secure Settings ENABLED_ACCESSIBILITY_SERVICES
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        if (enabledServicesSetting.contains("SteeringKeyAccessibilityService")) {
            return true
        }

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null) {
                val className = enabledComponent.className
                val pkgName = enabledComponent.packageName
                if (className.contains("SteeringKeyAccessibilityService") &&
                    (pkgName == context.packageName || pkgName == "com.byd.toolkit.gb" || pkgName == "com.byd.toolkit.gb.debug")
                ) {
                    return true
                }
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
        Toast.makeText(this, "Đang tự động cấp toàn bộ quyền qua Local ADB...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            val success = com.byd.toolkit.gb.util.AdbHelper.grantAccessibilityLocal(this@MainActivity)
            if (success) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Cấp quyền Local ADB thành công! Đã mở khóa Trợ Năng & Cửa Sổ Nổi.", Toast.LENGTH_LONG).show()
                    refreshPermissionStatus()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Local ADB chưa mở port 5555. Sẽ thử mở Cài đặt...", Toast.LENGTH_SHORT).show()
                    tryOpenAccessibilitySettingsUI()
                }
            }
        }
    }

    private fun tryOpenAccessibilitySettingsUI() {
        try {
            // Cách 1: Thử mở bằng Intent chuẩn
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            try {
                // Cách 2: BYD giấu intent chuẩn, thử force mở component ẩn của Cài đặt Trợ Năng
                val hiddenIntent = Intent().apply {
                    setClassName("com.android.settings", "com.android.settings.Settings\$AccessibilitySettingsActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(hiddenIntent)
                Toast.makeText(this, "Đã dùng thủ thuật mở Trợ Năng ẩn của hệ thống!", Toast.LENGTH_SHORT).show()
            } catch (e2: Exception) {
                try {
                    // Cách 3: Fallback ra ngoài Cài đặt tổng
                    val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(fallbackIntent)
                    Toast.makeText(this, "Hệ thống khoá chặt. Vui lòng kéo xuống tìm mục 'Trợ năng (Accessibility)'", Toast.LENGTH_LONG).show()
                } catch (e3: Exception) {
                    Toast.makeText(this, "Không thể mở Cài đặt. Vui lòng tự vào Cài đặt xe", Toast.LENGTH_LONG).show()
                }
            }
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
