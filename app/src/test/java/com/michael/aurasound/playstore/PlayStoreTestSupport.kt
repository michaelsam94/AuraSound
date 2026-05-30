package com.michael.aurasound.playstore

import com.michael.aurasound.core.data.model.MixChannel
import com.michael.aurasound.core.data.model.Preset
import com.michael.aurasound.core.data.model.SleepTimerState
import com.michael.aurasound.core.data.model.SoundTrack
import com.michael.aurasound.core.data.repository.MixerRepository
import com.michael.aurasound.core.data.repository.PresetRepository
import com.michael.aurasound.core.data.repository.SoundRepository
import com.michael.aurasound.core.data.repository.TimerRepository
import com.michael.aurasound.data.audio.SoundRepositoryImpl
import com.michael.aurasound.feature.mixer.MixerViewModel
import com.michael.aurasound.feature.presets.PresetsViewModel
import com.michael.aurasound.feature.timer.TimerViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory fakes so screenshots show realistic, populated content without the real
 * audio engine / DataStore (which would hang or need a device under Robolectric).
 */

/** Real catalog data — no Android dependencies, safe to reuse directly. */
private val soundCatalog: SoundRepository = SoundRepositoryImpl()

private fun track(id: String): SoundTrack =
    soundCatalog.getAllSounds().first { it.id == id }

class FakeMixerRepository(
    initialChannels: List<MixChannel> = emptyList(),
    playing: Boolean = false,
    master: Float = 1.0f,
) : MixerRepository {
    private val _channels = MutableStateFlow(initialChannels)
    private val _playing = MutableStateFlow(playing)
    private val _master = MutableStateFlow(master)

    override val activeChannels: StateFlow<List<MixChannel>> = _channels
    override val isPlaying: StateFlow<Boolean> = _playing
    override val masterVolume: StateFlow<Float> = _master

    override fun addChannel(track: SoundTrack, volume: Float) {
        _channels.value = _channels.value + MixChannel(track, volume)
    }

    override fun removeChannel(trackId: String) {
        _channels.value = _channels.value.filterNot { it.soundTrack.id == trackId }
    }

    override fun setVolume(trackId: String, volume: Float) {
        _channels.value = _channels.value.map {
            if (it.soundTrack.id == trackId) it.copy(volume = volume) else it
        }
    }

    override fun setMasterVolume(volume: Float) { _master.value = volume }
    override fun playAll() { _playing.value = true }
    override fun pauseAll() { _playing.value = false }
    override fun clearMix() { _channels.value = emptyList() }
    override fun loadMix(channels: List<MixChannel>) { _channels.value = channels }
}

class FakePresetRepository(initial: List<Preset> = emptyList()) : PresetRepository {
    private val _presets = MutableStateFlow(initial)
    override fun getPresets(): Flow<List<Preset>> = _presets
    override suspend fun savePreset(name: String, channels: List<MixChannel>) {
        _presets.value = _presets.value + Preset(id = name, name = name, channels = channels)
    }
    override suspend fun deletePreset(id: String) {
        _presets.value = _presets.value.filterNot { it.id == id }
    }
}

class FakeTimerRepository(initial: SleepTimerState = SleepTimerState()) : TimerRepository {
    private val _state = MutableStateFlow(initial)
    override val timerState: StateFlow<SleepTimerState> = _state
    override fun startTimer(durationMinutes: Int, fadeOutMinutes: Int) {
        _state.value = SleepTimerState(true, durationMinutes, fadeOutMinutes, durationMinutes * 60L)
    }
    override fun cancelTimer() { _state.value = SleepTimerState() }
}

/** Demo mix used across screenshots so the store listing looks alive. */
private val demoMix = listOf(
    MixChannel(track("rain_light"), 0.7f),
    MixChannel(track("campfire"), 0.45f),
    MixChannel(track("forest_birds"), 0.3f),
)

private val demoPresets = listOf(
    Preset("cozy_cabin", "Cozy Cabin", listOf(
        MixChannel(track("campfire"), 0.6f),
        MixChannel(track("rain_heavy"), 0.5f),
        MixChannel(track("soft_wind"), 0.35f),
    )),
    Preset("deep_focus", "Deep Focus", listOf(
        MixChannel(track("brown_noise"), 0.65f),
        MixChannel(track("coffee_shop"), 0.4f),
    )),
    Preset("ocean_sleep", "Ocean Sleep", listOf(
        MixChannel(track("ocean_waves"), 0.7f),
        MixChannel(track("night_crickets"), 0.3f),
        MixChannel(track("deep_drone"), 0.25f),
    )),
)

fun seededMixerViewModel(): MixerViewModel =
    MixerViewModel(
        mixerRepository = FakeMixerRepository(demoMix, playing = true, master = 0.8f),
        soundRepository = soundCatalog,
    )

fun seededPresetsViewModel(): PresetsViewModel =
    PresetsViewModel(
        presetRepository = FakePresetRepository(demoPresets),
        mixerRepository = FakeMixerRepository(demoMix, playing = true, master = 0.8f),
    )

fun runningTimerViewModel(): TimerViewModel =
    TimerViewModel(
        timerRepository = FakeTimerRepository(
            SleepTimerState(isRunning = true, durationMinutes = 45, fadeOutMinutes = 10, remainingSeconds = 33L * 60),
        ),
    )

fun demoMixChannels(): List<MixChannel> = demoMix
