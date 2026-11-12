package com.zakshub.nexus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.VerificationStatus

object NexusColors {
    val Background = Color(0xFF0B0F17)
    val Surface = Color(0xFF131B2E)
    val SurfaceElevated = Color(0xFF1B253D)
    val Primary = Color(0xFF4FD1C5)
    val Secondary = Color(0xFF63B3ED)
    val TextPrimary = Color(0xFFF7FAFC)
    val TextSecondary = Color(0xFFA0AEC0)

    // Distinct Environment Accent Colors (Never merge Termux and Debian proot)
    val EnvTermuxGreen = Color(0xFF10B981)
    val EnvDebianPurple = Color(0xFFA855F7)
    val EnvWindowsBlue = Color(0xFF3B82F6)
    val EnvAndroidSlate = Color(0xFF94A3B8)

    // Safety / Impact Accent Colors
    val ImpactSafeGreen = Color(0xFF22C55E)
    val ImpactConfigAmber = Color(0xFFF59E0B)
    val ImpactCautionRed = Color(0xFFEF4444)
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

private val NexusDarkColorScheme = darkColorScheme(
    primary = NexusColors.Primary,
    secondary = NexusColors.Secondary,
    background = NexusColors.Background,
    surface = NexusColors.Surface,
    surfaceVariant = NexusColors.SurfaceElevated,
    onPrimary = Color(0xFF042F2E),
    onBackground = NexusColors.TextPrimary,
    onSurface = NexusColors.TextPrimary,
    onSurfaceVariant = NexusColors.TextSecondary
)

@Composable
fun NexusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NexusDarkColorScheme,
        content = content
    )
}
