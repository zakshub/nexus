package com.zakshub.nexus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakshub.nexus.data.StarterKnowledgeCatalog
import com.zakshub.nexus.domain.model.CommandRecipe
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.security.SecretGuardValidator
import com.zakshub.nexus.ui.components.BadgePill
import com.zakshub.nexus.ui.components.ExecutionContextCard
import com.zakshub.nexus.ui.theme.NexusColors
import com.zakshub.nexus.ui.theme.accentColor
import kotlinx.coroutines.launch

enum class NexusTab(val label: String, val icon: ImageVector) {
    RECALL("Recall", Icons.Default.Home),
    LIBRARY("Library", Icons.AutoMirrored.Filled.MenuBook),
    TROUBLESHOOT("Troubleshoot", Icons.Default.Build),
    SYSTEMS("Systems", Icons.Default.Memory)
}

@Composable
fun NexusAppShell() {
    var selectedTab by remember { mutableStateOf(NexusTab.RECALL) }
    var selectedEnvironmentFilter by remember { mutableStateOf<ExecutionEnvironmentType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val completedWorkflowStepNumbers = remember { mutableStateListOf<Int>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val copyHandler: (CommandRecipe) -> Unit = { recipe ->
        clipboardManager.setText(AnnotatedString(recipe.commandSyntax))
        scope.launch {
            snackbarHostState.showSnackbar(
                "Copied for ${recipe.environmentType.badgeLabel} (${recipe.targetDeviceName})"
            )
        }
    }

    Scaffold(
        containerColor = NexusColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = NexusColors.Surface) {
                NexusTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NexusTopHeader(
                selectedEnv = selectedEnvironmentFilter,
                onSelectEnv = { selectedEnvironmentFilter = it }
            )

            when (selectedTab) {
                NexusTab.RECALL -> RecallScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedEnv = selectedEnvironmentFilter,
                    completedSteps = completedWorkflowStepNumbers,
                    onOpenWorkflowTab = { selectedTab = NexusTab.LIBRARY },
                    onCopyCommand = copyHandler
                )
                NexusTab.LIBRARY -> LibraryScreen(
                    selectedEnv = selectedEnvironmentFilter,
                    completedSteps = completedWorkflowStepNumbers,
                    onToggleStep = { stepNum ->
                        if (completedWorkflowStepNumbers.contains(stepNum)) {
                            completedWorkflowStepNumbers.remove(stepNum)
                        } else {
                            completedWorkflowStepNumbers.add(stepNum)
                        }
                    },
                    onCopyCommand = copyHandler
                )
                NexusTab.TROUBLESHOOT -> TroubleshootScreen(
                    selectedEnv = selectedEnvironmentFilter
                )
                NexusTab.SYSTEMS -> SystemsScreen()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NexusTopHeader(
    selectedEnv: ExecutionEnvironmentType?,
    onSelectEnv: (ExecutionEnvironmentType?) -> Unit
) {
    Surface(
        color = NexusColors.Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "NEXUS · Personal Technical Memory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NexusColors.Primary
            )
            Text(
                text = "Filter by target environment so you never run a command in the wrong shell:",
                style = MaterialTheme.typography.bodySmall,
                color = NexusColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedEnv == null,
                    onClick = { onSelectEnv(null) },
                    label = { Text("All Environments") }
                )
                listOf(
                    ExecutionEnvironmentType.TERMUX_HOST,
                    ExecutionEnvironmentType.DEBIAN_PROOT,
                    ExecutionEnvironmentType.WINDOWS_POWERSHELL
                ).forEach { env ->
                    FilterChip(
                        selected = selectedEnv == env,
                        onClick = { onSelectEnv(if (selectedEnv == env) null else env) },
                        label = { Text(env.shortLabel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecallScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedEnv: ExecutionEnvironmentType?,
    completedSteps: List<Int>,
    onOpenWorkflowTab: () -> Unit,
    onCopyCommand: (CommandRecipe) -> Unit
) {
    val filteredRecipes = StarterKnowledgeCatalog.recipes.filter { recipe ->
        val matchesEnv = selectedEnv == null || recipe.environmentType == selectedEnv
        val matchesSearch = searchQuery.isBlank() ||
            recipe.title.contains(searchQuery, ignoreCase = true) ||
            recipe.purposePlainEnglish.contains(searchQuery, ignoreCase = true) ||
            recipe.commandSyntax.contains(searchQuery, ignoreCase = true)
        matchesEnv && matchesSearch
    }

    val workflow = StarterKnowledgeCatalog.workflows.first()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search commands, SSH port 8022, Debian proot, or errors...") },
                singleLine = true
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.SurfaceElevated),
                border = BorderStroke(1.dp, NexusColors.Primary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Active Step-by-Step Guide (${completedSteps.size} of ${workflow.steps.size} Steps Done)",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workflow.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.TextPrimary
                    )
                    Text(
                        text = workflow.summaryPlainEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onOpenWorkflowTab) {
                        Text("Open Step-by-Step Checklist")
                    }
                }
            }
        }

        item {
            Text(
                text = "Verified Command Cards (${filteredRecipes.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NexusColors.TextPrimary
            )
        }

        items(filteredRecipes, key = { it.id }) { recipe ->
            ExecutionContextCard(
                recipe = recipe,
                onCopyCommand = onCopyCommand
            )
        }
    }
}

@Composable
private fun LibraryScreen(
    selectedEnv: ExecutionEnvironmentType?,
    completedSteps: List<Int>,
    onToggleStep: (Int) -> Unit,
    onCopyCommand: (CommandRecipe) -> Unit
) {
    val workflow = StarterKnowledgeCatalog.workflows.first()
    val recipes = StarterKnowledgeCatalog.recipes.filter {
        selectedEnv == null || it.environmentType == selectedEnv
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Guided Checklist: ${workflow.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NexusColors.TextPrimary
            )
            Text(
                text = "Check off each step as you go. Each step clearly states which device and shell to use.",
                style = MaterialTheme.typography.bodySmall,
                color = NexusColors.TextSecondary
            )
        }

        items(workflow.steps, key = { it.stepNumber }) { step ->
            val isDone = completedSteps.contains(step.stepNumber)
            val envColor = step.environmentType.accentColor()
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.Surface),
                border = BorderStroke(1.dp, envColor.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { onToggleStep(step.stepNumber) }
                        )
                        Column {
                            Text(
                                text = "Step ${step.stepNumber}: ${step.title}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NexusColors.TextPrimary
                            )
                            BadgePill(
                                text = "${step.targetDeviceName} · ${step.environmentType.badgeLabel}",
                                color = envColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = step.explanationPlainEnglish,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = step.commandToRun,
                        fontFamily = FontFamily.Monospace,
                        color = NexusColors.Primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF070A10), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Expected: ${step.expectedResult}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextPrimary
                    )
                    Text(
                        text = "If it fails: ${step.ifItFailsHint}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Saved Command Recipes (${recipes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NexusColors.TextPrimary
            )
        }

        items(recipes, key = { it.id }) { recipe ->
            ExecutionContextCard(
                recipe = recipe,
                onCopyCommand = onCopyCommand
            )
        }
    }
}

@Composable
private fun TroubleshootScreen(selectedEnv: ExecutionEnvironmentType?) {
    val cases = StarterKnowledgeCatalog.troubleshootingCases.filter {
        selectedEnv == null || it.environmentType == selectedEnv
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Symptom → Why It Happens → Safe Fix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NexusColors.TextPrimary
            )
            Text(
                text = "Proven solutions grounded in your Pixel 7 Pro (Termux / Debian) and Windows 11 setup.",
                style = MaterialTheme.typography.bodySmall,
                color = NexusColors.TextSecondary
            )
        }

        items(cases, key = { it.id }) { item ->
            val envColor = item.environmentType.accentColor()
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.Surface),
                border = BorderStroke(1.dp, envColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BadgePill(
                        text = "${item.targetDeviceName} · ${item.environmentType.badgeLabel}",
                        color = envColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.symptomTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Error seen: ${item.exactErrorSnippet}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.ImpactCautionRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Why this happens: ${item.plainEnglishWhyItHappens}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexusColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Safe Read-Only Check: `${item.safeReadOnlyCheckCommand}`",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.ImpactSafeGreen
                    )
                    Text(
                        text = "2. Verified Fix Command: `${item.verifiedFixCommand}`",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemsScreen() {
    var candidateNote by remember { mutableStateOf("") }
    val validation = remember(candidateNote) { SecretGuardValidator.validate(candidateNote) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "My Devices & Execution Environments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NexusColors.TextPrimary
            )
            Text(
                text = "Termux Host and Debian proot are tracked separately so commands never mix.",
                style = MaterialTheme.typography.bodySmall,
                color = NexusColors.TextSecondary
            )
        }

        items(StarterKnowledgeCatalog.environments, key = { it.id }) { env ->
            val envColor = env.type.accentColor()
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.Surface),
                border = BorderStroke(1.dp, envColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BadgePill(
                        text = "${env.deviceName} · ${env.type.badgeLabel}",
                        color = envColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = env.shellSummary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = env.filesystemBoundaryNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Important Boundary Rule: ${env.commonPitfallWarning}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.ImpactConfigAmber
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.SurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Secret Guard Live Protection Check",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.Primary
                    )
                    Text(
                        text = "NEXUS automatically blocks passwords, SSH private keys, or API tokens from being saved in regular notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = candidateNote,
                        onValueChange = { candidateNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Test pasting a note or command to verify safety...") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (validation.isSafe) {
                        BadgePill(
                            text = "Safe to Save — No Passwords or Private Keys Detected",
                            color = NexusColors.ImpactSafeGreen
                        )
                    } else {
                        validation.findings.forEach { finding ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                BadgePill(
                                    text = "Blocked: ${finding.ruleName}",
                                    color = NexusColors.ImpactCautionRed
                                )
                                Text(
                                    text = "${finding.userFriendlyWarning} Use placeholder: ${finding.safePlaceholderSuggestion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NexusColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
