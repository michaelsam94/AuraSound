package com.michael.aurasound.data.audio

import com.michael.aurasound.core.data.model.MixChannel
import com.michael.aurasound.core.data.model.SoundTrack
import com.michael.aurasound.core.data.repository.MixerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MixerRepositoryImpl(private val mixerEngine: MixerEngine) : MixerRepository {
    private val _activeChannels = MutableStateFlow<List<MixChannel>>(emptyList())
    override val activeChannels = _activeChannels.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying = _isPlaying.asStateFlow()

    private val _masterVolume = MutableStateFlow(1.0f)
    override val masterVolume = _masterVolume.asStateFlow()

    override fun addChannel(track: SoundTrack, volume: Float) {
        val current = _activeChannels.value.toMutableList()
        if (current.any { it.soundTrack.id == track.id }) return
        if (current.size >= 4) return // Enforce absolute limit of 4 active channels!
        
        val newChannel = MixChannel(track, volume)
        current.add(newChannel)
        _activeChannels.value = current
        
        mixerEngine.addChannel(SoundTrackData(track.id, track.assetPath), volume)
        
        // If already playing, immediately play the new channel
        if (_isPlaying.value) {
            mixerEngine.playAll()
        }
    }

    override fun removeChannel(trackId: String) {
        val current = _activeChannels.value.toMutableList()
        current.removeAll { it.soundTrack.id == trackId }
        _activeChannels.value = current
        
        mixerEngine.removeChannel(trackId)
    }

    override fun setVolume(trackId: String, volume: Float) {
        val current = _activeChannels.value.map { channel ->
            if (channel.soundTrack.id == trackId) {
                channel.copy(volume = volume)
            } else {
                channel
            }
        }
        _activeChannels.value = current
        mixerEngine.setVolume(trackId, volume)
    }

    override fun setMasterVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _masterVolume.value = clamped
        mixerEngine.setMasterVolume(clamped)
    }

    override fun playAll() {
        if (_activeChannels.value.isEmpty()) return
        _isPlaying.value = true
        mixerEngine.playAll()
    }

    override fun pauseAll() {
        _isPlaying.value = false
        mixerEngine.pauseAll()
    }

    override fun clearMix() {
        _activeChannels.value = emptyList()
        _isPlaying.value = false
        mixerEngine.clearMix()
    }

    override fun loadMix(channels: List<MixChannel>) {
        clearMix()
        val limited = channels.take(4)
        limited.forEach { addChannel(it.soundTrack, it.volume) }
        playAll()
    }
}
