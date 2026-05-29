package com.example.core.data.model

data class SoundTrack(
    val id: String,
    val name: String,
    val category: String, // "Focus", "Nature", "Ambient", "Sleep"
    val assetPath: String, // Relative to assets/ sounds/focus/white_noise.ogg
    val iconName: String   // String mapping to Material Icons
)

data class MixChannel(
    val soundTrack: SoundTrack,
    val volume: Float // 0f to 1f
)

data class SoundMix(
    val channels: List<MixChannel> = emptyList()
) {
    val isPlaying: Boolean
        get() = channels.isNotEmpty()
}

data class Preset(
    val id: String,
    val name: String,
    val channels: List<MixChannel>,
    val createdAt: Long = System.currentTimeMillis()
)

data class SleepTimerState(
    val isRunning: Boolean = false,
    val durationMinutes: Int = 0,
    val fadeOutMinutes: Int = 0,
    val remainingSeconds: Long = 0L
)
