package com.cutwa.videoeditor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cutwa.videoeditor.R
import com.cutwa.videoeditor.data.model.VideoProject
import com.cutwa.videoeditor.engine.VideoProcessor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@AndroidEntryPoint
class VideoProcessingService : Service() {
    
    @Inject
    lateinit var videoProcessor: VideoProcessor
    
    private val binder = VideoProcessingBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState
    
    private val _progress = MutableStateFlow(VideoProcessor.ProcessingProgress(0f, "", 0))
    val progress: StateFlow<VideoProcessor.ProcessingProgress> = _progress
    
    companion object {
        private const val CHANNEL_ID = "video_processing_channel"
        private const val NOTIFICATION_ID = 1
    }
    
    sealed class ProcessingState {
        object Idle : ProcessingState()
        object Processing : ProcessingState()
        object Completed : ProcessingState()
        data class Error(val message: String) : ProcessingState()
    }
    
    inner class VideoProcessingBinder : Binder() {
        fun getService(): VideoProcessingService = this@VideoProcessingService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Video processing service started")
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
    
    fun startVideoProcessing(project: VideoProject, outputPath: String) {
        if (_processingState.value is ProcessingState.Processing) {
            return // Already processing
        }
        
        _processingState.value = ProcessingState.Processing
        
        serviceScope.launch {
            try {
                val result = videoProcessor.processVideo(
                    project = project,
                    outputPath = outputPath,
                    onProgress = { progress ->
                        _progress.value = progress
                        updateNotification("Processing: ${progress.percentage.toInt()}%")
                    }
                )
                
                result.fold(
                    onSuccess = { 
                        _processingState.value = ProcessingState.Completed
                        updateNotification("Video processing completed")
                    },
                    onFailure = { error ->
                        _processingState.value = ProcessingState.Error(error.message ?: "Unknown error")
                        updateNotification("Video processing failed")
                    }
                )
            } catch (e: Exception) {
                _processingState.value = ProcessingState.Error(e.message ?: "Unknown error")
                updateNotification("Video processing failed")
            }
        }
    }
    
    fun cancelProcessing() {
        serviceScope.coroutineContext.cancelChildren()
        _processingState.value = ProcessingState.Idle
        updateNotification("Video processing cancelled")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Video Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for video processing progress"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cutwa Video Editor")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_video_processing)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}