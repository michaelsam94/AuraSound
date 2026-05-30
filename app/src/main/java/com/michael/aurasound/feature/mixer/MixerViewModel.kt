package com.michael.aurasound.feature.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michael.aurasound.ServiceLocator
import com.michael.aurasound.core.data.model.MixChannel
import com.michael.aurasound.core.data.model.SoundTrack
import com.michael.aurasound.core.data.repository.MixerRepository
import com.michael.aurasound.core.data.repository.SoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MixerUiState(
    val availableSounds: List<SoundTrack> = emptyList(),
    val filteredSounds: List<SoundTrack> = emptyList(),
    val activeChannels: List<MixChannel> = emptyList(),
    val selectedCategory: String = "All",
    val isPlaying: Boolean = false,
    val masterVolume: Float = 1.0f
)

class MixerViewModel(
    private val mixerRepository: MixerRepository = ServiceLocator.mixerRepository,
    private val soundRepository: SoundRepository = ServiceLocator.soundRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")

    val uiState: StateFlow<MixerUiState> = combine(
        _selectedCategory,
        mixerRepository.activeChannels,
        mixerRepository.isPlaying,
        mixerRepository.masterVolume
    ) { category, active, playing, masterVol ->
        val allSounds = soundRepository.getAllSounds()
        val filtered = if (category == "All") {
            allSounds
        } else {
            soundRepository.getSoundsByCategory(category)
        }
        MixerUiState(
            availableSounds = allSounds,
            filteredSounds = filtered,
            activeChannels = active,
            selectedCategory = category,
            isPlaying = playing,
            masterVolume = masterVol
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MixerUiState()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addChannel(track: SoundTrack, volume: Float = 0.5f) {
        mixerRepository.addChannel(track, volume)
    }

    fun removeChannel(trackId: String) {
        mixerRepository.removeChannel(trackId)
    }

    fun setChannelVolume(trackId: String, volume: Float) {
        mixerRepository.setVolume(trackId, volume)
    }

    fun setMasterVolume(volume: Float) {
        mixerRepository.setMasterVolume(volume)
    }

    fun togglePlayback() {
        if (uiState.value.isPlaying) {
            mixerRepository.pauseAll()
        } else {
            mixerRepository.playAll()
        }
    }

    fun clearMix() {
        mixerRepository.clearMix()
    }
}
