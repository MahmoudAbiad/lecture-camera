package com.mahmoud.lecturecamera

import android.app.Application
import com.mahmoud.lecturecamera.util.ErrorLogger

class LectureCameraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ErrorLogger.init(this)
    }
}
