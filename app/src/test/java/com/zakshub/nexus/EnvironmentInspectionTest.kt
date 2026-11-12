package com.zakshub.nexus

import com.zakshub.nexus.inspect.EnvironmentInspector
import com.zakshub.nexus.inspect.PortStatus
import com.zakshub.nexus.inspect.TermuxBridge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvironmentInspectionTest {

    @Test
    fun `probeLocalPort safely returns CLOSED or UNREACHABLE for inactive port without hanging`() = runBlocking {
        // Probe an arbitrary port (e.g. 59999) that is virtually guaranteed not to be listening
        val status = EnvironmentInspector.probeLocalPort(port = 59999, timeoutMs = 200)
        assertTrue(
            "Expected port to be CLOSED or UNREACHABLE, got $status",
            status == PortStatus.CLOSED || status == PortStatus.UNREACHABLE
        )
        assertFalse(status.isHealthy)
    }

    @Test
    fun `termux bridge strictly allows only verified read-only inspection commands`() {
        val allowed = listOf(
            "pgrep -l sshd",
            "whoami",
            "uptime",
            "cat /etc/os-release",
            "df -h",
            "free -h",
            "termux-wake-lock && sshd"
        )

        allowed.forEach { cmd ->
            assertTrue("Expected command '$cmd' to be allowed", TermuxBridge.isCommandAllowed(cmd))
            val spec = TermuxBridge.createCommandSpec(cmd)
            assertNotNull("Expected valid spec for '$cmd'", spec)
            assertEquals("com.termux.RUN_COMMAND", spec?.action)
            assertEquals("com.termux", spec?.packageName)
        }
    }

    @Test
    fun `termux bridge strictly rejects destructive and state-changing commands`() {
        val dangerous = listOf(
            "rm -rf /",
            "mkfs.ext4 /dev/sda",
            "reboot",
            "chmod 777 /",
            "apt install nginx",
            "pkill -9 -u 0",
            ":(){ :|:& };:"
        )

        dangerous.forEach { cmd ->
            assertFalse("Expected dangerous command '$cmd' to be rejected", TermuxBridge.isCommandAllowed(cmd))
            val spec = TermuxBridge.createCommandSpec(cmd)
            assertNull("Expected null spec for rejected command '$cmd'", spec)
        }
    }
}
