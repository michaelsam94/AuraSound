package com.michael.aurasound.data.timer

import com.michael.aurasound.core.data.model.SleepTimerState
import com.michael.aurasound.core.data.repository.MixerRepository
import com.michael.aurasound.core.data.repository.TimerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerRepositoryImpl(
    private val mixerRepository: MixerRepository
) : TimerRepository {
    private val _timerState = MutableStateFlow(SleepTimerState())
    override val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun startTimer(durationMinutes: Int, fadeOutMinutes: Int) {
        cancelTimer() // Cancel any active timer first

        val totalSeconds = durationMinutes * 60L
        _timerState.value = SleepTimerState(
            isRunning = true,
            durationMinutes = durationMinutes,
            fadeOutMinutes = fadeOutMinutes,
            remainingSeconds = totalSeconds
        )

        timerJob = scope.launch {
            var secondsLeft = totalSeconds
            val fadeOutSeconds = (fadeOutMinutes * 60).toFloat()
            val initialVolume = mixerRepository.masterVolume.value

            while (secondsLeft > 0) {
                delay(1000L)
                secondsLeft--
                
                // If fade-out period is active, smoothly reduce master volume
                if (fadeOutMinutes > 0 && secondsLeft <= fadeOutSeconds && fadeOutSeconds > 0) {
                    val progress = (secondsLeft.toFloat() / fadeOutSeconds).coerceIn(0f, 1f)
                    mixerRepository.setMasterVolume(progress * initialVolume)
                }

                _timerState.value = _timerState.value.copy(
                    remainingSeconds = secondsLeft
                )
            }

            // Completed! Stop audio, reset master volume, and clear state
            mixerRepository.pauseAll()
            mixerRepository.setMasterVolume(initialVolume)
            _timerState.value = SleepTimerState()
        }
    }

    override fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        mixerRepository.setMasterVolume(1.0f)
        _timerState.value = SleepTimerState()
    }
}
