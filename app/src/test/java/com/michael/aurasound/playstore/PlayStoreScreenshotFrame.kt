package com.michael.aurasound.playstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michael.aurasound.feature.mixer.MixerScreen
import com.michael.aurasound.feature.presets.PresetsScreen
import com.michael.aurasound.feature.timer.TimerScreen
import com.michael.aurasound.ui.theme.MyApplicationTheme

enum class PlayStoreScene { Mixer, Timer, Presets, SavePreset }

@Composable
fun PlayStoreScreenshotFrame(scene: PlayStoreScene) {
    MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (scene) {
                PlayStoreScene.Mixer -> MixerScreen(
                    viewModel = seededMixerViewModel(),
                    onTimerClick = {},
                    onPresetsClick = {},
                    animationsEnabled = false,
                )

                PlayStoreScene.Timer -> TimerScreen(
                    viewModel = runningTimerViewModel(),
                    onBackClick = {},
                )

                PlayStoreScene.Presets -> PresetsScreen(
                    viewModel = seededPresetsViewModel(),
                    onBackClick = {},
                )

                PlayStoreScene.SavePreset -> SavePresetOverlay()
            }
        }
    }
}

/**
 * Static stand-in for the "Save Current Mix" AlertDialog. Real `AlertDialog` can idle-loop
 * for 60s+ under Roborazzi, so we render a seeded presets screen, a scrim, and a dialog card
 * that mirrors the production styling.
 */
@Composable
private fun SavePresetOverlay() {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        PresetsScreen(
            viewModel = seededPresetsViewModel(),
            onBackClick = {},
        )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("Save Current Mix", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        "Give a unique name to identify this custom sound proportions.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = "Rainy Campfire",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = {}) { Text("Cancel") }
                        androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) { Text("Save") }
                    }
                }
            }
        }
    }
}
