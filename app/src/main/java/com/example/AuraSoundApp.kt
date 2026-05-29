package com.example

import android.app.Application

class AuraSoundApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }

    override fun onTerminate() {
        ServiceLocator.release()
        super.onTerminate()
    }
}
