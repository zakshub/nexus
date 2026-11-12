package com.zakshub.nexus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zakshub.nexus.domain.model.CommandRecipe
import com.zakshub.nexus.domain.model.VerificationStatus
import com.zakshub.nexus.ui.theme.NexusColors
import com.zakshub.nexus.ui.theme.accentColor

/**
 * The 7-Part "Execution Context Card" designed for a visual UX/UI designer:
 * 1. Target Device Pill
 * 2. Target Environment Badge (strictly separated Termux vs Debian proot vs Windows 11)
 * 3. Where-to-Run & Prompt Check Strip
 * 4. Plain-English Explanation + Expandable Flag Breakdown
 * 5. Expected Output Preview
 * 6. Verification Trust Badge
 * 7. Safety / Impact Level Badge
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExecutionContextCard(
    recipe: CommandRecipe,
    onCopyCommand: (CommandRecipe) -> Unit,
    onToggleVerification: ((CommandRecipe) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showFlagBreakdown by remember { mutableStateOf(false) }
    val envColor = recipe.environmentType.accentColor()
    val impactColor = recipe.impactLevel.accentColor()
    val verifyColor = recipe.verificationStatus.accentColor()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NexusColors.Surface),
        border = BorderStroke(1.dp, envColor.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1 & 2. Device Pill + Environment Badge
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgePill(
                    text = "Device: ${recipe.targetDeviceName}",
                    color = NexusColors.Secondary
                )
                BadgePill(
                    text = "Run In: ${recipe.environmentType.badgeLabel}",
                    color = envColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NexusColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Plain-English Explanation
            Text(
                text = recipe.purposePlainEnglish,
                style = MaterialTheme.typography.bodyMedium,
                color = NexusColors.TextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Where-to-Run & Prompt Check Strip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NexusColors.SurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Before running, check your screen prompt shows: ${recipe.environmentType.defaultPrompt}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = envColor
                    )
                    Text(
                        text = "How to get here: ${recipe.environmentType.switchInstruction}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Command Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexusColors.TerminalBg, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = recipe.commandSyntax,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NexusColors.TerminalText
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Expected Result Box
            Text(
                text = "Expected Result: ${recipe.expectedOutputPlainEnglish}",
                style = MaterialTheme.typography.bodySmall,
                color = NexusColors.TextPrimary
            )

            recipe.rollbackOrUndoHint?.let { undo ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "How to Undo / Exit: $undo",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6 & 7. Verification Trust Badge + Safety Impact Badge
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgePill(
                    text = recipe.verificationStatus.badgeLabel,
                    color = verifyColor
                )
                BadgePill(
                    text = "${recipe.impactLevel.badgeLabel} — ${recipe.impactLevel.plainEnglishSummary}",
                    color = impactColor
                )
            }

            // Expandable Parameter Breakdown
            AnimatedVisibility(visible = showFlagBreakdown) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(NexusColors.SurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Word-by-Word Command Breakdown:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.TextPrimary
                    )
                    recipe.parameters.forEach { param ->
                        Text(
                            text = "• ${param.token}  →  ${param.meaningPlainEnglish}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexusColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onCopyCommand(recipe) },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Copy (${recipe.environmentType.shortLabel})",
                        fontWeight = FontWeight.Bold
                    )
                }

                onToggleVerification?.let { onToggle ->
                    OutlinedButton(
                        onClick = { onToggle(recipe) }
                    ) {
                        Text(
                            text = if (recipe.verificationStatus == VerificationStatus.VERIFIED) "Unverify" else "Mark Verified",
                            color = if (recipe.verificationStatus == VerificationStatus.VERIFIED) NexusColors.ImpactSafeGreen else NexusColors.TextPrimary
                        )
                    }
                }

                if (recipe.parameters.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showFlagBreakdown = !showFlagBreakdown }
                    ) {
                        Text(
                            text = if (showFlagBreakdown) "Hide" else "Explain",
                            color = NexusColors.TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgePill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
