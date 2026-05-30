package com.michael.aurasound

import android.content.Context
import com.michael.aurasound.core.common.coroutines.DefaultDispatcherProvider
import com.michael.aurasound.core.common.coroutines.DispatcherProvider
import com.michael.aurasound.core.data.repository.MixerRepository
import com.michael.aurasound.core.data.repository.PresetRepository
import com.michael.aurasound.core.data.repository.SoundRepository
import com.michael.aurasound.core.data.repository.TimerRepository
import com.michael.aurasound.data.audio.MixerEngine
import com.michael.aurasound.data.audio.MixerRepositoryImpl
import com.michael.aurasound.data.audio.SoundRepositoryImpl
import com.michael.aurasound.data.presets.PresetRepositoryImpl
import com.michael.aurasound.data.timer.TimerRepositoryImpl

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
