package com.zakshub.nexus

import com.zakshub.nexus.ai.MobileTerminalAgent
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileTerminalAgentTest {

    @Test
    fun `creates storage exploration plan for Urdu or English downloads intent`() {
        val planEnglish = MobileTerminalAgent.createPlan("I want to explore my phone downloads folder in termux")
        assertEquals("Explore Phone Storage & Downloads in Termux", planEnglish.goalSummary)
        assertEquals(2, planEnglish.steps.size)
        assertEquals(ExecutionEnvironmentType.TERMUX_HOST, planEnglish.steps[0].targetEnvironment)
        assertTrue(planEnglish.steps[0].commandSyntax.contains("termux-setup-storage"))
        assertTrue(planEnglish.steps[1].commandSyntax.contains("downloads"))

        // Urdu Intent
        val planUrdu = MobileTerminalAgent.createPlan("مجھے ڈاؤن لوڈز والا فولڈر دیکھنا ہے")
        assertEquals("Explore Phone Storage & Downloads in Termux", planUrdu.goalSummary)
        assertEquals(2, planUrdu.steps.size)
        assertEquals(ExecutionEnvironmentType.TERMUX_HOST, planUrdu.steps[1].targetEnvironment)
    }

    @Test
    fun `creates two-stage transition plan for Debian PRoot intent`() {
        val plan = MobileTerminalAgent.createPlan("Debian کھول کے کام کرنا ہے")
        assertEquals("Two-Stage Transition: Enter Debian, Work in Linux, Return to Termux", plan.goalSummary)
        assertEquals(3, plan.steps.size)

        // Step 1: In Termux ($), launch Debian
        assertEquals(ExecutionEnvironmentType.TERMUX_HOST, plan.steps[0].targetEnvironment)
        assertEquals("~ $", plan.steps[0].expectedPrompt)
        assertTrue(plan.steps[0].commandSyntax.contains("proot-distro login debian"))

        // Step 2: In Debian (#), work in root
        assertEquals(ExecutionEnvironmentType.DEBIAN_PROOT, plan.steps[1].targetEnvironment)
        assertEquals("root@localhost:~#", plan.steps[1].expectedPrompt)
        assertTrue(plan.steps[1].commandSyntax.contains("/root"))

        // Step 3: Exit Debian back to Termux
        assertEquals(ExecutionEnvironmentType.DEBIAN_PROOT, plan.steps[2].targetEnvironment)
        assertEquals("exit", plan.steps[2].commandSyntax)
    }

    @Test
    fun `creates git status inspection plan for Debian repositories`() {
        val plan = MobileTerminalAgent.createPlan("پروجیکٹ کا گٹ اسٹیٹس چیک کرو")
        assertEquals("Check Git Status and Project Files in Debian PRoot", plan.goalSummary)
        assertEquals(2, plan.steps.size)
        assertEquals(ExecutionEnvironmentType.DEBIAN_PROOT, plan.steps[1].targetEnvironment)
        assertTrue(plan.steps[1].commandSyntax.contains("git status"))
    }

    @Test
    fun `blocks destructive command in user goal`() {
        val dangerous = MobileTerminalAgent.createPlan("rm -rf /")
        assertTrue(dangerous.goalSummary.contains("Blocked"))
        assertEquals(0, dangerous.steps.size)
        assertNotNull(dangerous.safetyNotice)
        assertTrue(dangerous.safetyNotice!!.contains("HIGH CAUTION"))
    }

    @Test
    fun `creates general safe fallback plan for arbitrary safe goal`() {
        val plan = MobileTerminalAgent.createPlan("inspect unknown custom tool")
        assertEquals(1, plan.steps.size)
        assertEquals(ExecutionEnvironmentType.TERMUX_HOST, plan.steps[0].targetEnvironment)
        assertEquals(VerificationStatus.AI_SUGGESTED, plan.provenance)
    }
}
