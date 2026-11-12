package com.zakshub.nexus.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.zakshub.nexus.inspect.EnvironmentInspector
import com.zakshub.nexus.inspect.LiveHealthReport
import com.zakshub.nexus.inspect.PortStatus
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zakshub.nexus.ai.AssistantDiagnosticResponse
import com.zakshub.nexus.ai.ContextRedactor
import com.zakshub.nexus.ai.DiagnosticQueryContext
import com.zakshub.nexus.ai.OfflineRetrievalProvider
import com.zakshub.nexus.data.StarterKnowledgeCatalog
import com.zakshub.nexus.data.backup.ImportResult
import com.zakshub.nexus.data.local.NexusDatabase
import com.zakshub.nexus.data.repository.NexusRepository
import com.zakshub.nexus.domain.model.CommandRecipe
import com.zakshub.nexus.domain.model.DevicePlatform
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.VerificationStatus
import com.zakshub.nexus.security.SecretGuardValidator
import com.zakshub.nexus.ui.components.BadgePill
import com.zakshub.nexus.ui.components.ExecutionContextCard
import com.zakshub.nexus.ui.theme.NexusColors
import com.zakshub.nexus.ui.theme.accentColor
import kotlinx.coroutines.launch
import java.util.UUID

enum class NexusTab(val label: String, val icon: ImageVector) {
    RECALL("Recall", Icons.Default.Home),
    LIBRARY("Library", Icons.AutoMirrored.Filled.MenuBook),
    TROUBLESHOOT("Troubleshoot", Icons.Default.Build),
    SYSTEMS("Systems", Icons.Default.Memory)
}

@Composable
fun NexusAppShell(
    repository: NexusRepository? = null
) {
    val context = LocalContext.current
    val actualRepository = remember {
        repository ?: NexusRepository(NexusDatabase.getInstance(context))
    }

    var selectedTab by remember { mutableStateOf(NexusTab.RECALL) }
    var selectedEnvironmentFilter by remember { mutableStateOf<ExecutionEnvironmentType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // Reactive State from Room Database (falling back to starter catalog while initializing)
    val dbRecipes by actualRepository.getAllRecipesFlow().collectAsState(initial = emptyList())
    val recipes = if (dbRecipes.isNotEmpty()) dbRecipes else StarterKnowledgeCatalog.recipes

    val dbEnvironments by actualRepository.getAllEnvironmentsFlow().collectAsState(initial = emptyList())
    val environments = if (dbEnvironments.isNotEmpty()) dbEnvironments else StarterKnowledgeCatalog.environments

    val dbCases by actualRepository.getAllCasesFlow().collectAsState(initial = emptyList())
    val cases = if (dbCases.isNotEmpty()) dbCases else StarterKnowledgeCatalog.troubleshootingCases

    val dbWorkflows by actualRepository.getAllWorkflowsFlow().collectAsState(initial = emptyList())
    val workflow = dbWorkflows.firstOrNull() ?: StarterKnowledgeCatalog.workflows.first()

    val copyHandler: (CommandRecipe) -> Unit = { recipe ->
        clipboardManager.setText(AnnotatedString(recipe.commandSyntax))
        scope.launch {
            snackbarHostState.showSnackbar(
                "Copied for ${recipe.environmentType.badgeLabel} (${recipe.targetDeviceName})"
            )
        }
    }

    val verifyHandler: (CommandRecipe) -> Unit = { recipe ->
        scope.launch {
            actualRepository.updateRecipeVerificationStatus(recipe.id, VerificationStatus.VERIFIED)
            snackbarHostState.showSnackbar("Marked '${recipe.title}' as Verified on Your Setup!")
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
                    recipes = recipes,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedEnv = selectedEnvironmentFilter,
                    workflow = workflow,
                    onOpenWorkflowTab = { selectedTab = NexusTab.LIBRARY },
                    onCopyCommand = copyHandler,
                    onToggleVerification = verifyHandler
                )
                NexusTab.LIBRARY -> LibraryScreen(
                    recipes = recipes,
                    selectedEnv = selectedEnvironmentFilter,
                    workflow = workflow,
                    onToggleStep = { stepNumber, isDone ->
                        scope.launch {
                            actualRepository.updateStepCompletion(workflow.id, stepNumber, isDone)
                        }
                    },
                    onAddRecipe = { newRecipe ->
                        scope.launch {
                            actualRepository.upsertRecipe(newRecipe)
                            snackbarHostState.showSnackbar("Saved new recipe to Memory Vault!")
                        }
                    },
                    onCopyCommand = copyHandler,
                    onToggleVerification = verifyHandler
                )
                NexusTab.TROUBLESHOOT -> TroubleshootScreen(
                    cases = cases,
                    recipes = recipes,
                    selectedEnv = selectedEnvironmentFilter,
                    onCopyCommand = { cmd, label ->
                        clipboardManager.setText(AnnotatedString(cmd))
                        scope.launch {
                            snackbarHostState.showSnackbar("Copied $label: $cmd")
                        }
                    }
                )
                NexusTab.SYSTEMS -> SystemsScreen(
                    environments = environments,
                    onExportJson = {
                        scope.launch {
                            val jsonString = actualRepository.exportJson()
                            clipboardManager.setText(AnnotatedString(jsonString))
                            snackbarHostState.showSnackbar("Exported Knowledge Vault JSON copied to clipboard!")
                        }
                    },
                    onExportMarkdown = {
                        scope.launch {
                            val mdString = actualRepository.exportMarkdown()
                            clipboardManager.setText(AnnotatedString(mdString))
                            snackbarHostState.showSnackbar("Exported Markdown Notebook copied to clipboard!")
                        }
                    },
                    onImportJson = { jsonString ->
                        scope.launch {
                            when (val result = actualRepository.importJson(jsonString)) {
                                is ImportResult.Success -> {
                                    snackbarHostState.showSnackbar(
                                        "Restored: ${result.recipesCount} recipes, ${result.casesCount} fixes, ${result.workflowsCount} workflows!"
                                    )
                                }
                                is ImportResult.BlockedBySecretGuard -> {
                                    snackbarHostState.showSnackbar("BLOCKED: ${result.violationSummary}")
                                }
                                is ImportResult.Failure -> {
                                    snackbarHostState.showSnackbar("Error: ${result.errorMessage}")
                                }
                            }
                        }
                    }
                )
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
    recipes: List<CommandRecipe>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedEnv: ExecutionEnvironmentType?,
    workflow: com.zakshub.nexus.domain.model.GuidedWorkflow,
    onOpenWorkflowTab: () -> Unit,
    onCopyCommand: (CommandRecipe) -> Unit,
    onToggleVerification: (CommandRecipe) -> Unit
) {
    val filteredRecipes = recipes.filter { recipe ->
        val matchesEnv = selectedEnv == null || recipe.environmentType == selectedEnv
        val matchesSearch = searchQuery.isBlank() ||
            recipe.title.contains(searchQuery, ignoreCase = true) ||
            recipe.purposePlainEnglish.contains(searchQuery, ignoreCase = true) ||
            recipe.commandSyntax.contains(searchQuery, ignoreCase = true)
        matchesEnv && matchesSearch
    }

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
                        text = "Active Step-by-Step Guide (${workflow.completedStepsCount} of ${workflow.steps.size} Steps Done)",
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

        val pinnedRecipes = recipes.filter { it.isPinnedDaily }
        if (pinnedRecipes.isNotEmpty() && searchQuery.isBlank() && selectedEnv == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexusColors.SurfaceElevated),
                    border = BorderStroke(1.dp, NexusColors.Secondary.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐ Pinned Daily Fast-Track",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NexusColors.Secondary
                            )
                            val currentContext = LocalContext.current
                            OutlinedButton(
                                onClick = {
                                    val launchIntent = currentContext.packageManager.getLaunchIntentForPackage("com.termux")
                                    if (launchIntent != null) {
                                        currentContext.startActivity(launchIntent)
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("Open Termux", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "One-tap copy for your daily startup and test routines:",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexusColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        pinnedRecipes.take(4).forEach { pinned ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pinned.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NexusColors.TextPrimary
                                    )
                                    Text(
                                        text = pinned.commandSyntax,
                                        fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NexusColors.Primary
                                    )
                                }
                                Button(
                                    onClick = { onCopyCommand(pinned) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("Copy", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
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
                onCopyCommand = onCopyCommand,
                onToggleVerification = onToggleVerification
            )
        }
    }
}

@Composable
private fun LibraryScreen(
    recipes: List<CommandRecipe>,
    selectedEnv: ExecutionEnvironmentType?,
    workflow: com.zakshub.nexus.domain.model.GuidedWorkflow,
    onToggleStep: (Int, Boolean) -> Unit,
    onAddRecipe: (CommandRecipe) -> Unit,
    onCopyCommand: (CommandRecipe) -> Unit,
    onToggleVerification: (CommandRecipe) -> Unit
) {
    var showAddRecipeDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newCommand by remember { mutableStateOf("") }
    var newPurpose by remember { mutableStateOf("") }
    var newEnv by remember { mutableStateOf(ExecutionEnvironmentType.TERMUX_HOST) }

    val filteredRecipes = recipes.filter {
        selectedEnv == null || it.environmentType == selectedEnv
    }

    val secretValidation = remember(newCommand, newPurpose) {
        SecretGuardValidator.validate("$newCommand $newPurpose")
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Guided Checklist: ${workflow.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.TextPrimary
                    )
                    Text(
                        text = "Check off each step. Automatically saved to your phone's memory vault.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                }
                Button(
                    onClick = { showAddRecipeDialog = !showAddRecipeDialog },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Text("Add Recipe")
                }
            }
        }

        if (showAddRecipeDialog) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexusColors.SurfaceElevated),
                    border = BorderStroke(1.dp, NexusColors.Primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Save New Command to Memory Vault",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NexusColors.Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Title (e.g., Check Debian Disk Free)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newCommand,
                            onValueChange = { newCommand = it },
                            label = { Text("Exact Command Syntax") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newPurpose,
                            onValueChange = { newPurpose = it },
                            label = { Text("Plain-English Purpose (What it does)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (!secretValidation.isSafe) {
                            Text(
                                text = "BLOCKED BY SECRET GUARD: Passwords or private keys cannot be saved in notes.",
                                color = NexusColors.ImpactCautionRed,
                                style = MaterialTheme.typography.labelSmall
                            )
                        } else {
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank() && newCommand.isNotBlank()) {
                                        val deviceName = if (newEnv == ExecutionEnvironmentType.WINDOWS_POWERSHELL) "Windows 11 Laptop" else "Google Pixel 7 Pro"
                                        val platform = if (newEnv == ExecutionEnvironmentType.WINDOWS_POWERSHELL) DevicePlatform.WINDOWS else DevicePlatform.ANDROID
                                        onAddRecipe(
                                            CommandRecipe(
                                                id = "custom-${UUID.randomUUID()}",
                                                title = newTitle,
                                                targetDeviceName = deviceName,
                                                targetPlatform = platform,
                                                environmentType = newEnv,
                                                purposePlainEnglish = newPurpose,
                                                commandSyntax = newCommand,
                                                parameters = emptyList(),
                                                expectedOutputPlainEnglish = "Custom command saved by user.",
                                                impactLevel = ImpactLevel.CONFIG_CHANGE,
                                                verificationStatus = VerificationStatus.VERIFIED
                                            )
                                        )
                                        newTitle = ""
                                        newCommand = ""
                                        newPurpose = ""
                                        showAddRecipeDialog = false
                                    }
                                },
                                enabled = newTitle.isNotBlank() && newCommand.isNotBlank() && secretValidation.isSafe,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save into Vault")
                            }
                        }
                    }
                }
            }
        }

        items(workflow.steps, key = { it.stepNumber }) { step ->
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
                            checked = step.isCompleted,
                            onCheckedChange = { onToggleStep(step.stepNumber, it) }
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
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Saved Command Recipes (${filteredRecipes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NexusColors.TextPrimary
            )
        }

        items(filteredRecipes, key = { it.id }) { recipe ->
            ExecutionContextCard(
                recipe = recipe,
                onCopyCommand = onCopyCommand,
                onToggleVerification = onToggleVerification
            )
        }
    }
}

@Composable
private fun TroubleshootScreen(
    cases: List<com.zakshub.nexus.domain.model.TroubleshootingCase>,
    recipes: List<CommandRecipe> = emptyList(),
    selectedEnv: ExecutionEnvironmentType?,
    onCopyCommand: (String, String) -> Unit = { _, _ -> }
) {
    val filteredCases = cases.filter {
        selectedEnv == null || it.environmentType == selectedEnv
    }

    val assistantProvider = remember { OfflineRetrievalProvider() }
    val scope = rememberCoroutineScope()

    var assistantQuestion by remember { mutableStateOf("") }
    var assistantErrorLog by remember { mutableStateOf("") }
    var diagnosticResult by remember { mutableStateOf<AssistantDiagnosticResponse?>(null) }
    var showContextPreview by remember { mutableStateOf(false) }
    var previewPromptText by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.SurfaceElevated),
                border = BorderStroke(1.dp, NexusColors.Primary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = NexusColors.Primary
                        )
                        Text(
                            text = "  Ask Technical Assistant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NexusColors.Primary
                        )
                    }
                    Text(
                        text = "100% offline retrieval grounded in your verified notes and troubleshooting history. Never leaks passwords or private keys.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = assistantQuestion,
                        onValueChange = { assistantQuestion = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("What technical issue or question do you have?") },
                        placeholder = { Text("e.g. SSH connection timed out on port 8022") }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = assistantErrorLog,
                        onValueChange = { assistantErrorLog = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Terminal error snippet (optional)") },
                        placeholder = { Text("e.g. Connection refused or command not found") }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val queryCtx = DiagnosticQueryContext(
                                    userQuestion = assistantQuestion,
                                    errorSnippet = assistantErrorLog.ifBlank { null },
                                    selectedEnvironment = selectedEnv,
                                    availableVerifiedRecipes = recipes,
                                    knownCases = cases
                                )
                                previewPromptText = ContextRedactor.buildPreviewPrompt(queryCtx)
                                showContextPreview = !showContextPreview
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (showContextPreview) "Hide Preview" else "Preview Context")
                        }

                        Button(
                            onClick = {
                                val queryCtx = DiagnosticQueryContext(
                                    userQuestion = assistantQuestion,
                                    errorSnippet = assistantErrorLog.ifBlank { null },
                                    selectedEnvironment = selectedEnv,
                                    availableVerifiedRecipes = recipes,
                                    knownCases = cases
                                )
                                scope.launch {
                                    diagnosticResult = assistantProvider.diagnose(queryCtx)
                                }
                            },
                            enabled = assistantQuestion.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Primary)
                        ) {
                            Text("Diagnose Safe Fix")
                        }
                    }

                    AnimatedVisibility(visible = showContextPreview) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "Redacted Context (Secret Guard Verified):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NexusColors.Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = previewPromptText,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                color = NexusColors.TextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF070A10), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            )
                        }
                    }

                    diagnosticResult?.let { res ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NexusColors.Surface),
                            border = BorderStroke(1.dp, NexusColors.Primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BadgePill(
                                        text = if (res.verificationStatus == VerificationStatus.VERIFIED) "Verified in Personal Memory" else "✨ AI Suggestion — Unverified",
                                        color = if (res.verificationStatus == VerificationStatus.VERIFIED) NexusColors.ImpactSafeGreen else NexusColors.ImpactConfigAmber
                                    )
                                    BadgePill(
                                        text = res.targetEnvironment.badgeLabel,
                                        color = res.targetEnvironment.accentColor()
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = res.plainEnglishDiagnosis,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NexusColors.TextPrimary
                                )

                                res.highRiskWarning?.let { warning ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = warning,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NexusColors.ImpactCautionRed,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(NexusColors.ImpactCautionRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    )
                                }

                                res.suggestedCommand?.let { cmd ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cmd,
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NexusColors.Primary,
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color(0xFF070A10), RoundedCornerShape(6.dp))
                                                .padding(8.dp)
                                        )
                                        Button(
                                            onClick = { onCopyCommand(cmd, "Suggested Command") },
                                            colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Primary),
                                            modifier = Modifier.padding(start = 6.dp)
                                        ) {
                                            Text("Copy", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                if (res.citations.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Vault Citations (${res.citations.size}):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NexusColors.TextSecondary
                                    )
                                    res.citations.forEach { citation ->
                                        Text(
                                            text = "• [${citation.environmentType}] ${citation.title}: ${citation.relevanceSnippet}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NexusColors.TextSecondary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Proven Troubleshooting Cases (${filteredCases.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NexusColors.TextPrimary
            )
            Text(
                text = "Direct solutions grounded in your Pixel 7 Pro (Termux / Debian) and Windows 11 setup.",
                style = MaterialTheme.typography.bodySmall,
                color = NexusColors.TextSecondary
            )
        }

        items(filteredCases, key = { it.id }) { item ->
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
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1. Safe Check: `${item.safeReadOnlyCheckCommand}`",
                                style = MaterialTheme.typography.bodySmall,
                                color = NexusColors.ImpactSafeGreen
                            )
                        }
                        OutlinedButton(
                            onClick = { onCopyCommand(item.safeReadOnlyCheckCommand, "Safe Check") },
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text("Copy Check", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "2. Verified Fix: `${item.verifiedFixCommand}`",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = NexusColors.Primary
                            )
                        }
                        Button(
                            onClick = { onCopyCommand(item.verifiedFixCommand, "Fix Command") },
                            colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Primary),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text("Copy Fix", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemsScreen(
    environments: List<com.zakshub.nexus.domain.model.EnvironmentProfile>,
    onExportJson: () -> Unit,
    onExportMarkdown: () -> Unit,
    onImportJson: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var candidateNote by remember { mutableStateOf("") }
    val validation = remember(candidateNote) { SecretGuardValidator.validate(candidateNote) }

    var importPayloadText by remember { mutableStateOf("") }
    var showImportBox by remember { mutableStateOf(false) }

    var healthReport by remember { mutableStateOf<LiveHealthReport?>(null) }
    var isProbing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        healthReport = EnvironmentInspector.inspectAll(context)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.SurfaceElevated),
                border = BorderStroke(1.dp, NexusColors.Primary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 Live Environment Inspector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NexusColors.Primary
                        )
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isProbing = true
                                    healthReport = EnvironmentInspector.inspectAll(context)
                                    isProbing = false
                                }
                            },
                            enabled = !isProbing,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(if (isProbing) "Probing..." else "Re-probe", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(
                        text = "Real-time non-blocking read-only inspection of localhost SSH daemon and local network state.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val port = healthReport?.sshPortStatus ?: PortStatus.UNREACHABLE
                    val portColor = if (port == PortStatus.OPEN) NexusColors.ImpactSafeGreen else NexusColors.ImpactCautionRed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Termux SSH (Port 8022)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = NexusColors.TextPrimary
                            )
                            Text(
                                text = port.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = portColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        BadgePill(
                            text = if (port == PortStatus.OPEN) "Ready for SSH" else "Not Running",
                            color = portColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val wifiIp = healthReport?.detectedWifiIp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Detected Phone Wi-Fi IP",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = NexusColors.TextPrimary
                            )
                            Text(
                                text = wifiIp ?: "No active Wi-Fi detected",
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (wifiIp != null) NexusColors.Primary else NexusColors.TextSecondary
                            )
                        }
                        if (wifiIp != null) {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(wifiIp))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Primary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Copy IP", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    healthReport?.batteryPercent?.let { percent ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Battery & Power",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = NexusColors.TextPrimary
                            )
                            Text(
                                text = "$percent% · ${if (healthReport?.isCharging == true) "Charging ⚡" else "On Battery"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }

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

        items(environments, key = { it.id }) { env ->
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
                        text = "Backup & Knowledge Export / Restore",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.Primary
                    )
                    Text(
                        text = "Save your technical memory vault as clean JSON or Markdown, or restore from a backup file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexusColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onExportJson,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "JSON")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Export JSON")
                        }
                        OutlinedButton(
                            onClick = onExportMarkdown,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export Markdown")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showImportBox = !showImportBox },
                        colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Import")
                        Text(if (showImportBox) "Hide Restore Box" else "Restore from Backup JSON")
                    }

                    AnimatedVisibility(visible = showImportBox) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = importPayloadText,
                                onValueChange = { importPayloadText = it },
                                label = { Text("Paste Backup JSON here...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    if (importPayloadText.isNotBlank()) {
                                        onImportJson(importPayloadText)
                                        importPayloadText = ""
                                        showImportBox = false
                                    }
                                },
                                enabled = importPayloadText.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Scan with Secret Guard & Restore")
                            }
                        }
                    }
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
