package com.zakshub.nexus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.VerificationStatus

object NexusColors {
    // 2026 Light-First Color System
    val Background = Color(0xFFF8FAFC)        // Crisp light off-white (Slate 50)
    val Surface = Color(0xFFFFFFFF)           // Clean card white
    val SurfaceElevated = Color(0xFFF1F5F9)   // Subtle container / input field fill (Slate 100)
    val Primary = Color(0xFF1A73E8)           // Vibrant Brand Blue
    val PrimaryContainer = Color(0xFFE8F0FE)  // Soft Blue tint for active pills
    val Secondary = Color(0xFF4F46E5)         // Rich Indigo
    val TextPrimary = Color(0xFF0F172A)       // High contrast deep slate (Slate 900)
    val TextSecondary = Color(0xFF64748B)     // Refined secondary text (Slate 500)
    val TextTertiary = Color(0xFF94A3B8)      // Subtle caption text (Slate 400)
    val BorderSubtle = Color(0xFFE2E8F0)      // Crisp light border (Slate 200)

    // Status Banner Colors (All Systems Online)
    val StatusOnlineBg = Color(0xFFECFDF5)    // Emerald 50
    val StatusOnlineBorder = Color(0xFFA7F3D0)// Emerald 200
    val StatusOnlineText = Color(0xFF065F46)  // Emerald 800
    val StatusOnlineIcon = Color(0xFF10B981)  // Emerald 500

    // Distinct Environment Accent Colors (Never merge Termux and Debian proot)
    val EnvTermuxGreen = Color(0xFF059669)    // Rich Emerald
    val EnvDebianPurple = Color(0xFF7C3AED)   // Modern Violet
    val EnvWindowsBlue = Color(0xFF2563EB)    // Vibrant Blue
    val EnvAndroidSlate = Color(0xFF475569)   // Sophisticated Slate

    // Safety / Impact Accent Colors
    val ImpactSafeGreen = Color(0xFF16A34A)   // Green 600
    val ImpactConfigAmber = Color(0xFFD97706) // Amber 600
    val ImpactCautionRed = Color(0xFFDC2626)  // Red 600

    // Code block container (Crisp terminal block)
    val TerminalBg = Color(0xFF0F172A)        // Deep slate for code blocks
    val TerminalText = Color(0xFF38BDF8)      // Sky blue syntax highlight
}

fun ExecutionEnvironmentType.accentColor(): Color = when (this) {
    ExecutionEnvironmentType.TERMUX_HOST -> NexusColors.EnvTermuxGreen
    ExecutionEnvironmentType.DEBIAN_PROOT -> NexusColors.EnvDebianPurple
    ExecutionEnvironmentType.WINDOWS_POWERSHELL -> NexusColors.EnvWindowsBlue
    ExecutionEnvironmentType.ANDROID_SYSTEM -> NexusColors.EnvAndroidSlate
}

fun ImpactLevel.accentColor(): Color = when (this) {
    ImpactLevel.READ_ONLY -> NexusColors.ImpactSafeGreen
    ImpactLevel.CONFIG_CHANGE -> NexusColors.ImpactConfigAmber
    ImpactLevel.HIGH_CAUTION -> NexusColors.ImpactCautionRed
}

fun VerificationStatus.accentColor(): Color = when (this) {
    VerificationStatus.VERIFIED -> NexusColors.ImpactSafeGreen
    VerificationStatus.UNVERIFIED_DRAFT -> NexusColors.ImpactConfigAmber
    VerificationStatus.AI_SUGGESTED -> NexusColors.EnvDebianPurple
}

private val NexusLightColorScheme = lightColorScheme(
    primary = NexusColors.Primary,
    onPrimary = Color.White,
    primaryContainer = NexusColors.PrimaryContainer,
    onPrimaryContainer = NexusColors.Primary,
    secondary = NexusColors.Secondary,
    onSecondary = Color.White,
    background = NexusColors.Background,
    onBackground = NexusColors.TextPrimary,
    surface = NexusColors.Surface,
    onSurface = NexusColors.TextPrimary,
    surfaceVariant = NexusColors.SurfaceElevated,
    onSurfaceVariant = NexusColors.TextSecondary,
    outline = NexusColors.BorderSubtle
)

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexusLightColorScheme,
        content = content
    )
}
