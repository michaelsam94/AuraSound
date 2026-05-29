package com.example.feature.presets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ServiceLocator
import com.example.core.data.model.MixChannel
import com.example.core.data.model.Preset
import com.example.core.data.repository.MixerRepository
import com.example.core.data.repository.PresetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PresetsUiState(
    val presets: List<Preset> = emptyList(),
    val currentActiveMix: List<MixChannel> = emptyList()
)

class PresetsViewModel(
    private val presetRepository: PresetRepository = ServiceLocator.presetRepository,
    private val mixerRepository: MixerRepository = ServiceLocator.mixerRepository
) : ViewModel() {

    val uiState: StateFlow<PresetsUiState> = combine(
        presetRepository.getPresets(),
        mixerRepository.activeChannels
    ) { presetsList, activeMix ->
        PresetsUiState(
            presets = presetsList,
            currentActiveMix = activeMix
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PresetsUiState()
    )

    fun saveCurrentMixAsPreset(name: String) {
        viewModelScope.launch {
            val currentMix = mixerRepository.activeChannels.value
            if (currentMix.isNotEmpty() && name.isNotBlank()) {
                presetRepository.savePreset(name.trim(), currentMix)
            }
        }
    }

    fun deletePreset(id: String) {
        viewModelScope.launch {
            presetRepository.deletePreset(id)
        }
    }

    fun loadPreset(preset: Preset) {
        mixerRepository.loadMix(preset.channels)
    }
}
