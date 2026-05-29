package com.example

import android.content.Context
import com.example.core.common.coroutines.DefaultDispatcherProvider
import com.example.core.common.coroutines.DispatcherProvider
import com.example.core.data.repository.MixerRepository
import com.example.core.data.repository.PresetRepository
import com.example.core.data.repository.SoundRepository
import com.example.core.data.repository.TimerRepository
import com.example.data.audio.MixerEngine
import com.example.data.audio.MixerRepositoryImpl
import com.example.data.audio.SoundRepositoryImpl
import com.example.data.presets.PresetRepositoryImpl
import com.example.data.timer.TimerRepositoryImpl

object ServiceLocator {
    private var mixerEngineInstance: MixerEngine? = null
    
    val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
    
    lateinit var soundRepository: SoundRepository
    lateinit var mixerRepository: MixerRepository
    lateinit var presetRepository: PresetRepository
    lateinit var timerRepository: TimerRepository

    fun init(context: Context) {
        if (mixerEngineInstance == null) {
            val engine = MixerEngine(context.applicationContext)
            mixerEngineInstance = engine
            
            soundRepository = SoundRepositoryImpl()
            mixerRepository = MixerRepositoryImpl(engine)
            presetRepository = PresetRepositoryImpl(context.applicationContext, soundRepository)
            timerRepository = TimerRepositoryImpl(mixerRepository)
        }
    }

    fun release() {
        mixerEngineInstance?.release()
        mixerEngineInstance = null
    }
}
