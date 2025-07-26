package com.cutwa.videoeditor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CutwaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any global configurations here
        setupVideoProcessing()
    }
    
    private fun setupVideoProcessing() {
        // Initialize video processing libraries and configurations
        // This can include FFmpeg initialization, OpenGL context setup, etc.
    }
}