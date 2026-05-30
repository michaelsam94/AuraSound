package com.michael.aurasound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.michael.aurasound.feature.mixer.MixerScreen
import com.michael.aurasound.feature.mixer.MixerViewModel
import com.michael.aurasound.feature.presets.PresetsScreen
import com.michael.aurasound.feature.presets.PresetsViewModel
import com.michael.aurasound.feature.timer.TimerScreen
import com.michael.aurasound.feature.timer.TimerViewModel
import com.michael.aurasound.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuraSoundAppLayout()
                }
            }
        }
    }
}

@Composable
fun AuraSoundAppLayout() {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    // Shared VM instances across split pane representation
    val mixerViewModel: MixerViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()
    val presetsViewModel: PresetsViewModel = viewModel()

    if (isTablet) {
        // Widescreen Tablet Dashboard Layout (Canvas Side-by-Side Split)
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
            ) {
                MixerScreen(
                    viewModel = mixerViewModel,
                    onTimerClick = {}, // Hidden/Disabled in Tablet Split view list
                    onPresetsClick = {}
                )
            }
            
            VerticalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxWidth()
                ) {
                    TimerScreen(
                        viewModel = timerViewModel,
                        onBackClick = {}
                    )
                }
                
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    PresetsScreen(
                        viewModel = presetsViewModel,
                        onBackClick = {}
                    )
                }
            }
        }
    } else {
        // Mobile Sequential Type-Safe Flow Navigation Layout
        val navController = rememberNavController()
        
        NavHost(
            navController = navController,
            startDestination = "mixer",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("mixer") {
                MixerScreen(
                    viewModel = mixerViewModel,
                    onTimerClick = { navController.navigate("timer") },
                    onPresetsClick = { navController.navigate("presets") }
                )
            }
            composable("timer") {
                TimerScreen(
                    viewModel = timerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("presets") {
                PresetsScreen(
                    viewModel = presetsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
