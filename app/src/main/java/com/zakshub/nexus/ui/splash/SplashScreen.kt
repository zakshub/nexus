package com.zakshub.nexus.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakshub.nexus.ui.components.NexusLogo
import com.zakshub.nexus.ui.theme.NexusColors

/**
 * 2026 Contemporary Light-First Splash Screen & Genuine Loading Experience.
 *
 * Requirements fulfilled:
 * - Clean light background (#F8FAFC).
 * - Proportionate, refined NEXUS logo (72.dp).
 * - Strong whitespace and balanced typography.
 * - Genuine loading stages only (no fake percentages or artificial delay).
 * - Seamless transition into Home Dashboard.
 */
@Composable
fun SplashScreen(
    statusMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexusColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Proportionate, clean NEXUS Ribbon Logo
            NexusLogo(size = 72.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // Refined 2026 Typography
            Text(
                text = "NEXUS",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = NexusColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Personal Technical Assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = NexusColors.TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Genuine loading stage indicator - visible only while active initialization is underway
            AnimatedVisibility(
                visible = statusMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.height(50.dp)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .width(130.dp)
                            .height(3.dp),
                        color = NexusColors.Primary,
                        trackColor = NexusColors.BorderSubtle,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusColors.TextTertiary
                    )
                }
            }
        }

        // Professional baseline brand signature
        Text(
            text = "Your Systems · Your Memory · Your Intelligence",
            style = MaterialTheme.typography.labelSmall,
            color = NexusColors.TextTertiary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}
