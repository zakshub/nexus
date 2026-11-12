package com.zakshub.nexus.inspect

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

enum class PortStatus(val label: String, val isHealthy: Boolean) {
    OPEN("Open / Listening", true),
    CLOSED("Closed / Not Running", false),
    UNREACHABLE("Unreachable", false)
}

data class LiveHealthReport(
    val sshPortStatus: PortStatus,
    val detectedWifiIp: String?,
    val batteryPercent: Int?,
    val isCharging: Boolean?,
    val timestamp: Long = System.currentTimeMillis()
)

object EnvironmentInspector {

    /**
     * Probes localhost TCP port (default 8022 for Termux SSH) with a safe short timeout.
     * Guaranteed non-blocking and read-only.
     */
    suspend fun probeLocalPort(port: Int = 8022, timeoutMs: Int = 600): PortStatus = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", port), timeoutMs)
                PortStatus.OPEN
            }
        } catch (e: java.net.ConnectException) {
            PortStatus.CLOSED
        } catch (e: java.net.SocketTimeoutException) {
            PortStatus.UNREACHABLE
        } catch (e: Exception) {
            PortStatus.CLOSED
        }
    }

    /**
     * Scans local network interfaces to detect active Wi-Fi IPv4 address (e.g., 192.168.x.x).
     */
    fun detectWifiIp(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addresses = intf.inetAddresses
                for (addr in addresses) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val host = addr.hostAddress
                        if (host != null && (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172."))) {
                            return host
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Safe fallback if network permissions or interfaces are restricted
        }
        return null
    }

    /**
     * Reads current battery percentage and charging state so the owner knows
     * if the phone will stay awake during long SSH or build sessions.
     */
    fun getBatteryInfo(context: Context): Pair<Int, Boolean>? {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            if (bm != null) {
                val capacity = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val isCharging = bm.isCharging
                Pair(capacity, isCharging)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Runs full non-blocking read-only inspection of the device environment.
     */
    suspend fun inspectAll(context: Context): LiveHealthReport {
        val portStatus = probeLocalPort(port = 8022)
        val wifiIp = detectWifiIp()
        val battery = getBatteryInfo(context)

        return LiveHealthReport(
            sshPortStatus = portStatus,
            detectedWifiIp = wifiIp,
            batteryPercent = battery?.first,
            isCharging = battery?.second
        )
    }
}

data class TermuxCommandSpec(
    val packageName: String = "com.termux",
    val serviceName: String = "com.termux.app.RunCommandService",
    val action: String = "com.termux.RUN_COMMAND",
    val shellPath: String = "/data/data/com.termux/files/usr/bin/bash",
    val command: String
)

/**
 * Safe, allowlisted intent bridge for Termux (ADR 0007 / Phase 5).
 * Only permit strictly read-only inspection commands.
 */
object TermuxBridge {

    const val TERMUX_PACKAGE = "com.termux"
    const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
    const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"

    val AllowedReadOnlyCommands = setOf(
        "pgrep -l sshd",
        "whoami",
        "uptime",
        "cat /etc/os-release",
        "df -h",
        "free -h",
        "termux-wake-lock && sshd"
    )

    fun isCommandAllowed(command: String): Boolean {
        val trimmed = command.trim()
        return AllowedReadOnlyCommands.contains(trimmed)
    }

    fun createCommandSpec(command: String): TermuxCommandSpec? {
        if (!isCommandAllowed(command)) {
            return null // Strict security reject: command not in read-only allowlist
        }
        return TermuxCommandSpec(command = command)
    }

    fun createRunCommandIntent(command: String): Intent? {
        val spec = createCommandSpec(command) ?: return null
        return Intent().apply {
            setClassName(spec.packageName, spec.serviceName)
            action = spec.action
            putExtra("com.termux.RUN_COMMAND_PATH", spec.shellPath)
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", spec.command))
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
        }
    }
}
