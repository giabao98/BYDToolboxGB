package com.byd.toolkit.gb.util

import android.util.Base64
import android.util.Log
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

object AdbHelper {
    private const val TAG = "AdbHelper"

    class AndroidBase64 : AdbBase64 {
        override fun encodeToString(data: ByteArray): String {
            return Base64.encodeToString(data, Base64.NO_WRAP)
        }
    }

    suspend fun grantAccessibilityLocal(): Boolean {
        return withContext(Dispatchers.IO) {
            var socket: Socket? = null
            var connection: AdbConnection? = null
            try {
                // Generate a dummy RSA key for the local ADB connection
                val crypto = AdbCrypto.generateAdbKeyPair(AndroidBase64())
                
                // Connect to local ADB daemon (must have Wireless Debugging enabled)
                socket = Socket("127.0.0.1", 5555)
                connection = AdbConnection.create(socket, crypto)
                
                // Set timeout to wait for user to click "Allow" on the car screen
                connection.connect()

                Log.i(TAG, "Local ADB connected!")

                // Execute the grant command
                val stream = connection.open("shell:settings put secure enabled_accessibility_services com.byd.toolkit.gb.debug/com.byd.toolkit.gb.service.SteeringKeyAccessibilityService")
                
                // Read output just to consume it
                val response = stream.read()
                Log.i(TAG, "ADB response length: ${response?.size}")

                stream.close()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute local ADB command", e)
                false
            } finally {
                try {
                    connection?.close()
                    socket?.close()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}
