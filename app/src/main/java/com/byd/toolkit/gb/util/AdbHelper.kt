package com.byd.toolkit.gb.util

import android.content.Context
import android.util.Base64
import android.util.Log
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object AdbHelper {
    private const val TAG = "AdbHelper"

    class AndroidBase64 : AdbBase64 {
        override fun encodeToString(data: ByteArray): String {
            return Base64.encodeToString(data, Base64.NO_WRAP)
        }
    }

    suspend fun grantAccessibilityLocal(context: Context? = null): Boolean {
        return withContext(Dispatchers.IO) {
            var socket: Socket? = null
            var connection: AdbConnection? = null
            try {
                // Generate a dummy RSA key for the local ADB connection
                val crypto = AdbCrypto.generateAdbKeyPair(AndroidBase64())

                // Connect to local ADB daemon (must have Wireless Debugging / TCP ADB on port 5555)
                socket = Socket()
                socket.connect(InetSocketAddress("127.0.0.1", 5555), 5000)
                connection = AdbConnection.create(socket, crypto)

                // Handshake with adbd
                connection.connect()
                Log.i(TAG, "Local ADB connection established successfully!")

                val currentPkg = context?.packageName ?: "com.byd.toolkit.gb"
                val serviceClass = "com.byd.toolkit.gb.service.SteeringKeyAccessibilityService"
                val packagesToGrant = setOf("com.byd.toolkit.gb", "com.byd.toolkit.gb.debug", currentPkg)

                val servicesList = packagesToGrant.joinToString(":") { "$it/$serviceClass" }

                // Build compound command:
                // 1. Register accessibility service
                // 2. Turn ON global accessibility toggle (CRITICAL for service startup)
                // 3. Grant overlay permission (SYSTEM_ALERT_WINDOW)
                // 4. Whitelist battery optimization
                // 5. Grant WRITE_SECURE_SETTINGS
                val commands = mutableListOf<String>()
                commands.add("settings put secure enabled_accessibility_services $servicesList")
                commands.add("settings put secure accessibility_enabled 1")

                for (pkg in packagesToGrant) {
                    commands.add("appops set $pkg SYSTEM_ALERT_WINDOW allow")
                    commands.add("dumpsys deviceidle whitelist +$pkg")
                    commands.add("pm grant $pkg android.permission.WRITE_SECURE_SETTINGS")
                }

                val fullCmd = commands.joinToString(" ; ")
                Log.i(TAG, "Executing ADB command: $fullCmd")

                val stream = connection.open("shell:$fullCmd")
                try {
                    val response = stream.read()
                    Log.i(TAG, "ADB command finished, response bytes: ${response?.size ?: 0}")
                } catch (e: Exception) {
                    Log.d(TAG, "Stream reading ended: ${e.message}")
                } finally {
                    try {
                        stream.close()
                    } catch (_: Exception) {}
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute local ADB command", e)
                false
            } finally {
                try {
                    connection?.close()
                    socket?.close()
                } catch (_: Exception) {}
            }
        }
    }
}
