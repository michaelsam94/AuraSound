package com.michael.aurasound.feature.mixer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michael.aurasound.core.data.model.MixChannel
import com.michael.aurasound.core.data.model.SoundTrack
import com.michael.aurasound.core.ui.components.PulsingOrb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixerScreen(
    viewModel: MixerViewModel,
    onTimerClick: () -> Unit,
    onPresetsClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient Moving Orbs
        PulsingOrb(
            modifier = Modifier.fillMaxSize(),
            isPlaying = state.isPlaying,
            masterVolume = state.masterVolume,
            animationsEnabled = animationsEnabled
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "AuraSound",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(
                            onClick = onTimerClick,
                            modifier = Modifier.testTag("timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Sleep Timer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onPresetsClick,
                            modifier = Modifier.testTag("presets_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Saved Presets",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                // Master Play/Pause Panel
                MasterPlaybackControl(
                    isPlaying = state.isPlaying,
                    masterVolume = state.masterVolume,
                    activeChannelsCount = state.activeChannels.size,
                    onPlayPauseToggle = { viewModel.togglePlayback() },
                    onMasterVolumeChange = { viewModel.setMasterVolume(it) },
                    onClearMix = { viewModel.clearMix() }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Section: Active channels
                AnimatedVisibility(
                    visible = state.activeChannels.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            text = "Active Mixer (${state.activeChannels.size}/4)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth().testTag("active_channels_list")
                        ) {
                            items(state.activeChannels, key = { it.soundTrack.id }) { channel ->
                                ActiveChannelCard(
                                    channel = channel,
                                    onVolumeChange = { volume -> viewModel.setChannelVolume(channel.soundTrack.id, volume) },
                                    onRemove = { viewModel.removeChannel(channel.soundTrack.id) }
                                )
                            }
                        }
                    }
                }

                // Section: Category Selection
                val categories = listOf("All", "Focus", "Nature", "Ambient", "Sleep")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Section: Sound Track Library
                Text(
                    text = "Sound Library",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (state.filteredSounds.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No ambient sounds available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sound_library_grid")
                    ) {
                        items(state.filteredSounds, key = { it.id }) { sound ->
                            val isAdded = state.activeChannels.any { it.soundTrack.id == sound.id }
                            SoundGridItem(
                                sound = sound,
                                isAdded = isAdded,
                                onClick = {
                                    if (isAdded) {
                                        viewModel.removeChannel(sound.id)
                                    } else {
                                        viewModel.addChannel(sound)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveChannelCard(
    channel: MixChannel,
    onVolumeChange: (Float) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        modifier = modifier
            .width(150.dp)
            .padding(2.dp)
            .testTag("active_channel_${channel.soundTrack.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getSoundIcon(channel.soundTrack.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Sound",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = channel.soundTrack.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(channel.volume * 100).toInt()}%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Slider(
                value = channel.volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SoundGridItem(
    sound: SoundTrack,
    isAdded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isAdded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "containerColor"
    )

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("sound_item_${sound.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isAdded) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getSoundIcon(sound.iconName),
                    contentDescription = null,
                    tint = if (isAdded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = sound.name,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = if (isAdded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MasterPlaybackControl(
    isPlaying: Boolean,
    masterVolume: Float,
    activeChannelsCount: Int,
    onPlayPauseToggle: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onClearMix: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Master Volume",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Master Control",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (activeChannelsCount > 0) {
                    TextButton(
                        onClick = onClearMix,
                        modifier = Modifier.testTag("clear_mix_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Mix", fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Slider(
                    value = masterVolume,
                    onValueChange = onMasterVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                        .testTag("master_volume_slider")
                )

                Button(
                    onClick = onPlayPauseToggle,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    enabled = activeChannelsCount > 0,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("play_pause_fab"),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Playback" else "Start Playback",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "PAUSE" else "PLAY",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

fun getSoundIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Waves" -> Icons.Default.Waves
        "Hearing" -> Icons.Default.Hearing
        "WaterDrop" -> Icons.Default.WaterDrop
        "Thunderstorm" -> Icons.Default.Thunderstorm
        "BeachAccess" -> Icons.Default.BeachAccess
        "Forest" -> Icons.Default.Forest
        "Air" -> Icons.Default.Air
        "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
        "LocalCafe" -> Icons.Default.LocalCafe
        "DirectionsRailway" -> Icons.Default.DirectionsRailway
        "Toys" -> Icons.Default.Toys
        "Bedtime" -> Icons.Default.Bedtime
        "Nightlight" -> Icons.Default.Nightlight
        "BugReport" -> Icons.Default.BugReport
        "Favorite" -> Icons.Default.Favorite
        else -> Icons.Default.MusicNote
    }
}
