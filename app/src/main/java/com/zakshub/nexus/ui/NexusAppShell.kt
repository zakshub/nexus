package com.zakshub.nexus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.sp
import com.zakshub.nexus.ui.components.NexusLogo
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

    var healthReport by remember { mutableStateOf<LiveHealthReport?>(null) }
    LaunchedEffect(Unit) {
        healthReport = EnvironmentInspector.inspectAll(context)
    }

    val openTermuxHandler: () -> Unit = {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.termux")
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Termux app is not installed on this Android device.")
            }
        }
    }

    val copyIpHandler: () -> Unit = {
        val ip = healthReport?.detectedWifiIp ?: EnvironmentInspector.detectWifiIp() ?: "127.0.0.1"
        clipboardManager.setText(AnnotatedString(ip))
        scope.launch {
            snackbarHostState.showSnackbar("Copied Device Wi-Fi IP: $ip")
        }
    }

    val refreshHealthHandler: () -> Unit = {
        scope.launch {
            healthReport = EnvironmentInspector.inspectAll(context)
            snackbarHostState.showSnackbar("Environment re-probed: Port 8022 is ${healthReport?.sshPortStatus?.label}")
        }
    }

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
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NexusColors.Primary,
                            selectedTextColor = NexusColors.Primary,
                            indicatorColor = NexusColors.PrimaryContainer,
                            unselectedIconColor = NexusColors.TextSecondary,
                            unselectedTextColor = NexusColors.TextSecondary
                        )
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
                onOpenNotifications = {
                    scope.launch {
                        snackbarHostState.showSnackbar("All systems operational. No unread alerts.")
                    }
                },
                onOpenProfile = {
                    scope.launch {
                        snackbarHostState.showSnackbar("NEXUS Technical Assistant · Connected to Pixel 7 Pro")
                    }
                }
            )

            when (selectedTab) {
                NexusTab.RECALL -> RecallScreen(
                    recipes = recipes,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedEnv = selectedEnvironmentFilter,
                    onSelectEnv = { selectedEnvironmentFilter = it },
                    workflow = workflow,
                    healthReport = healthReport,
                    onRefreshHealth = refreshHealthHandler,
                    onOpenTermux = openTermuxHandler,
                    onOpenWorkflowTab = { selectedTab = NexusTab.LIBRARY },
                    onCopyIp = copyIpHandler,
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

@Composable
private fun NexusTopHeader(
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    Surface(
        color = NexusColors.Surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NexusLogo(size = 34.dp)
                Column {
                    Text(
                        text = "NEXUS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.TextPrimary
                    )
                    Text(
                        text = "Personal Technical Assistant",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusColors.TextSecondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenNotifications) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = NexusColors.TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(NexusColors.PrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ZH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.Primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecallScreen(
    recipes: List<CommandRecipe>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedEnv: ExecutionEnvironmentType?,
    onSelectEnv: (ExecutionEnvironmentType?) -> Unit,
    workflow: com.zakshub.nexus.domain.model.GuidedWorkflow,
    healthReport: LiveHealthReport?,
    onRefreshHealth: () -> Unit,
    onOpenTermux: () -> Unit,
    onOpenWorkflowTab: () -> Unit,
    onCopyIp: () -> Unit,
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

    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when {
        currentHour < 12 -> "Good Morning! ✨"
        currentHour < 17 -> "Good Afternoon! ✨"
        else -> "Good Evening! ✨"
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Greeting
        item {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NexusColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your technical assistant is ready.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexusColors.TextSecondary
                )
            }
        }

        // 2. Pill Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search commands, solutions...",
                        color = NexusColors.TextTertiary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NexusColors.TextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = NexusColors.TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NexusColors.Surface,
                    unfocusedContainerColor = NexusColors.Surface,
                    focusedBorderColor = NexusColors.Primary,
                    unfocusedBorderColor = NexusColors.BorderSubtle
                )
            )
        }

        // 3. Live System Status Banner (Real Environment Inspector Data)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.StatusOnlineBg),
                border = BorderStroke(1.dp, NexusColors.StatusOnlineBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(NexusColors.StatusOnlineIcon, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Online",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        val isSshOpen = healthReport?.sshPortStatus == PortStatus.OPEN
                        Text(
                            text = if (isSshOpen) "All Systems Online" else "Local System Ready",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NexusColors.StatusOnlineText
                        )
                        val portLabel = healthReport?.sshPortStatus?.label ?: "Ready"
                        val ipLabel = healthReport?.detectedWifiIp?.let { " · IP: $it" } ?: ""
                        Text(
                            text = "Termux SSH (8022): $portLabel$ipLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexusColors.StatusOnlineText.copy(alpha = 0.85f)
                        )
                    }
                    IconButton(
                        onClick = onRefreshHealth,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = NexusColors.StatusOnlineText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 4. Quick Actions (4-card grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NexusColors.TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = ">_",
                        iconBg = Color(0xFFE0F2FE),
                        iconColor = Color(0xFF0284C7),
                        title = "Open Termux",
                        onClick = onOpenTermux
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = "⇄",
                        iconBg = Color(0xFFF3E8FF),
                        iconColor = Color(0xFF7C3AED),
                        title = "SSH Guide",
                        onClick = onOpenWorkflowTab
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = "📶",
                        iconBg = Color(0xFFECFDF5),
                        iconColor = Color(0xFF059669),
                        title = "Check IP Address",
                        onClick = onCopyIp
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = "✓",
                        iconBg = Color(0xFFFEF3C7),
                        iconColor = Color(0xFFD97706),
                        title = "Run Checklist",
                        onClick = onOpenWorkflowTab
                    )
                }
            }
        }

        // 5. Your Environments Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Environments",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.TextPrimary
                    )
                    if (selectedEnv != null) {
                        TextButton(
                            onClick = { onSelectEnv(null) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Show All",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusColors.Primary
                            )
                        }
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedEnv == null,
                        onClick = { onSelectEnv(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NexusColors.PrimaryContainer,
                            selectedLabelColor = NexusColors.Primary
                        )
                    )
                    listOf(
                        ExecutionEnvironmentType.WINDOWS_POWERSHELL,
                        ExecutionEnvironmentType.ANDROID_SYSTEM,
                        ExecutionEnvironmentType.TERMUX_HOST,
                        ExecutionEnvironmentType.DEBIAN_PROOT
                    ).forEach { env ->
                        FilterChip(
                            selected = selectedEnv == env,
                            onClick = { onSelectEnv(if (selectedEnv == env) null else env) },
                            label = { Text(env.shortLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = env.accentColor().copy(alpha = 0.15f),
                                selectedLabelColor = env.accentColor()
                            )
                        )
                    }
                }
            }
        }

        // 6. Active Step-by-Step Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusColors.Surface),
                border = BorderStroke(1.dp, NexusColors.Primary.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenWorkflowTab,
                        colors = ButtonDefaults.buttonColors(containerColor = NexusColors.Primary)
                    ) {
                        Text("Open Step-by-Step Checklist")
                    }
                }
            }
        }

        // 7. Pinned Daily Fast-Track
        val pinnedRecipes = recipes.filter { it.isPinnedDaily }
        if (pinnedRecipes.isNotEmpty() && searchQuery.isBlank() && selectedEnv == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexusColors.Surface),
                    border = BorderStroke(1.dp, NexusColors.Secondary.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                            OutlinedButton(
                                onClick = onOpenTermux,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("Open Termux", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "One-tap copy for your daily startup and test routines:",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexusColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
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

        // 8. Verified Command Cards
        item {
            Text(
                text = "Verified Actions (${filteredRecipes.size})",
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
private fun QuickActionCard(
    icon: String,
    iconBg: Color,
    iconColor: Color,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = NexusColors.Surface),
        border = BorderStroke(1.dp, NexusColors.BorderSubtle),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = iconColor
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = NexusColors.TextPrimary
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
