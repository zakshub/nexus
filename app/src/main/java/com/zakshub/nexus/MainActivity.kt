package com.zakshub.nexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.zakshub.nexus.data.local.NexusDatabase
import com.zakshub.nexus.data.repository.NexusRepository
import com.zakshub.nexus.inspect.EnvironmentInspector
import com.zakshub.nexus.ui.NexusAppShell
import com.zakshub.nexus.ui.splash.SplashScreen
import com.zakshub.nexus.ui.theme.NexusTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexusTheme {
                NexusRoot()
            }
        }
    }
}

@Composable
fun NexusRoot() {
    val context = LocalContext.current
    val repository = remember { NexusRepository(NexusDatabase.getInstance(context)) }

    var isReady by remember { mutableStateOf(false) }
    var stageMessage by remember { mutableStateOf<String?>("Initializing technical memory vault...") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Genuine initialization stages
            repository.getAllRecipesFlow().firstOrNull()
            stageMessage = "Checking local environments..."
            EnvironmentInspector.inspectAll(context)
        }
        // Seamless transition without artificial delays
        stageMessage = null
        isReady = true
    }

    Crossfade(
        targetState = isReady,
        animationSpec = tween(durationMillis = 350),
        label = "SplashToAppCrossfade"
    ) { ready ->
        if (!ready) {
            SplashScreen(statusMessage = stageMessage)
        } else {
            NexusAppShell(repository = repository)
        }
    }
}
