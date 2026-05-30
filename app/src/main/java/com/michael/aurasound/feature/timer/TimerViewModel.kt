package com.michael.aurasound.feature.timer

import androidx.lifecycle.ViewModel
import com.michael.aurasound.ServiceLocator
import com.michael.aurasound.core.data.model.SleepTimerState
import com.michael.aurasound.core.data.repository.TimerRepository
import kotlinx.coroutines.flow.StateFlow

class TimerViewModel(
    private val timerRepository: TimerRepository = ServiceLocator.timerRepository
) : ViewModel() {

    val timerState: StateFlow<SleepTimerState> = timerRepository.timerState

    fun startTimer(durationMinutes: Int, fadeOutMinutes: Int) {
        timerRepository.startTimer(durationMinutes, fadeOutMinutes)
    }

    fun cancelTimer() {
        timerRepository.cancelTimer()
    }
}
