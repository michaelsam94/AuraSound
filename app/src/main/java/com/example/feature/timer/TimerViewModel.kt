package com.example.feature.timer

import androidx.lifecycle.ViewModel
import com.example.ServiceLocator
import com.example.core.data.model.SleepTimerState
import com.example.core.data.repository.TimerRepository
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
