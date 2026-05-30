package com.michael.aurasound.core.data.repository

import com.michael.aurasound.core.data.model.MixChannel
import com.michael.aurasound.core.data.model.Preset
import com.michael.aurasound.core.data.model.SleepTimerState
import com.michael.aurasound.core.data.model.SoundTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SoundRepository {
    fun getAllSounds(): List<SoundTrack>
    fun getSoundsByCategory(category: String): List<SoundTrack>
}

interface MixerRepository {
    val activeChannels: StateFlow<List<MixChannel>>
    val isPlaying: StateFlow<Boolean>
    val masterVolume: StateFlow<Float>

    fun addChannel(track: SoundTrack, volume: Float)
    fun removeChannel(trackId: String)
    fun setVolume(trackId: String, volume: Float)
    fun setMasterVolume(volume: Float)
    fun playAll()
    fun pauseAll()
    fun clearMix()
    fun loadMix(channels: List<MixChannel>)
}

interface PresetRepository {
    fun getPresets(): Flow<List<Preset>>
    suspend fun savePreset(name: String, channels: List<MixChannel>)
    suspend fun deletePreset(id: String)
}

interface TimerRepository {
    val timerState: StateFlow<SleepTimerState>
    fun startTimer(durationMinutes: Int, fadeOutMinutes: Int)
    fun cancelTimer()
}
