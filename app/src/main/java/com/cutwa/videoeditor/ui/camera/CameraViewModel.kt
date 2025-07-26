package com.cutwa.videoeditor.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutwa.videoeditor.data.database.VideoProjectDao
import com.cutwa.videoeditor.data.model.*
import com.cutwa.videoeditor.util.VideoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val videoProjectDao: VideoProjectDao,
    private val videoUtils: VideoUtils
) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    private val _cameraFacing = MutableStateFlow(CameraFacing.BACK)
    val cameraFacing: StateFlow<CameraFacing> = _cameraFacing.asStateFlow()

    private val _flashMode = MutableStateFlow(FlashMode.OFF)
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

    private val _selectedSpeed = MutableStateFlow(1f)
    val selectedSpeed: StateFlow<Float> = _selectedSpeed.asStateFlow()

    private val _isGridEnabled = MutableStateFlow(false)
    val isGridEnabled: StateFlow<Boolean> = _isGridEnabled.asStateFlow()

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter.asStateFlow()

    private val _beautyLevel = MutableStateFlow(0f)
    val beautyLevel: StateFlow<Float> = _beautyLevel.asStateFlow()

    private var recordingTimerJob: Job? = null
    private var currentRecordingPath: String? = null

    fun startRecording() {
        if (_isRecording.value) return

        viewModelScope.launch {
            try {
                _isRecording.value = true
                _recordingDuration.value = 0L
                
                // Generate recording file path
                val projectId = UUID.randomUUID().toString()
                val projectDir = videoUtils.getProjectDir(projectId)
                if (!projectDir.exists()) {
                    projectDir.mkdirs()
                }
                
                currentRecordingPath = projectDir.resolve("recording_${System.currentTimeMillis()}.mp4").absolutePath
                
                // Start recording timer
                startRecordingTimer()
                
                // TODO: Start actual camera recording with CameraX
                // This would involve setting up video capture use case
                
            } catch (e: Exception) {
                e.printStackTrace()
                _isRecording.value = false
            }
        }
    }

    fun stopRecording(onComplete: (String) -> Unit) {
        if (!_isRecording.value) return

        viewModelScope.launch {
            try {
                _isRecording.value = false
                recordingTimerJob?.cancel()
                
                // TODO: Stop actual camera recording
                
                // Create project from recorded video
                val recordingPath = currentRecordingPath
                if (recordingPath != null) {
                    val projectId = createProjectFromRecording(recordingPath)
                    onComplete(projectId)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startRecordingTimer() {
        recordingTimerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (_isRecording.value) {
                _recordingDuration.value = System.currentTimeMillis() - startTime
                delay(100) // Update every 100ms
            }
        }
    }

    private suspend fun createProjectFromRecording(recordingPath: String): String {
        val projectId = UUID.randomUUID().toString()
        
        // Get video info
        val videoInfo = videoUtils.getVideoInfo(android.net.Uri.parse("file://$recordingPath"))
        
        // Generate thumbnail
        val thumbnailPath = videoUtils.generateThumbnail(
            android.net.Uri.parse("file://$recordingPath"), 
            projectId
        )
        
        // Create project
        val project = VideoProject(
            id = projectId,
            name = "Recording ${Date()}",
            thumbnailPath = thumbnailPath,
            duration = videoInfo.duration,
            aspectRatio = AspectRatio(videoInfo.width, videoInfo.height),
            resolution = Resolution(videoInfo.width, videoInfo.height),
            frameRate = videoInfo.frameRate,
            tracks = listOf(
                Track(
                    id = UUID.randomUUID().toString(),
                    type = TrackType.VIDEO,
                    items = listOf(
                        TrackItem(
                            id = UUID.randomUUID().toString(),
                            type = TrackItemType.VIDEO_CLIP,
                            startTime = 0L,
                            duration = videoInfo.duration,
                            sourceUri = "file://$recordingPath",
                            speed = _selectedSpeed.value
                        )
                    )
                )
            ),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // Save project to database
        videoProjectDao.insertProject(project)
        
        return projectId
    }

    fun flipCamera() {
        _cameraFacing.value = when (_cameraFacing.value) {
            CameraFacing.BACK -> CameraFacing.FRONT
            CameraFacing.FRONT -> CameraFacing.BACK
        }
        // TODO: Update CameraX camera selector
    }

    fun toggleFlash() {
        _flashMode.value = when (_flashMode.value) {
            FlashMode.OFF -> FlashMode.ON
            FlashMode.ON -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.OFF
        }
        // TODO: Update CameraX flash mode
    }

    fun setRecordingSpeed(speed: Float) {
        _selectedSpeed.value = speed
        // TODO: Adjust recording speed if currently recording
    }

    fun toggleGrid() {
        _isGridEnabled.value = !_isGridEnabled.value
    }

    fun selectFilter(filterId: String?) {
        _selectedFilter.value = filterId
        // TODO: Apply real-time filter to camera preview
    }

    fun setBeautyLevel(level: Float) {
        _beautyLevel.value = level.coerceIn(0f, 1f)
        // TODO: Apply beauty filter with specified intensity
    }

    fun setTimer(seconds: Int) {
        // TODO: Implement timer functionality
        viewModelScope.launch {
            if (seconds > 0) {
                delay(seconds * 1000L)
                startRecording()
            }
        }
    }

    fun takePhoto() {
        // TODO: Implement photo capture functionality
        viewModelScope.launch {
            try {
                // Capture photo using CameraX ImageCapture use case
                // Save to gallery and optionally add to current project
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addToExistingProject(projectId: String) {
        // TODO: Add current recording to an existing project
        viewModelScope.launch {
            try {
                val project = videoProjectDao.getProject(projectId)
                if (project != null && currentRecordingPath != null) {
                    // Add new video clip to project
                    val videoInfo = videoUtils.getVideoInfo(android.net.Uri.parse("file://$currentRecordingPath"))
                    
                    val newItem = TrackItem(
                        id = UUID.randomUUID().toString(),
                        type = TrackItemType.VIDEO_CLIP,
                        startTime = project.duration, // Add at the end
                        duration = videoInfo.duration,
                        sourceUri = "file://$currentRecordingPath",
                        speed = _selectedSpeed.value
                    )
                    
                    val videoTrack = project.tracks.find { it.type == TrackType.VIDEO }
                    if (videoTrack != null) {
                        val updatedTracks = project.tracks.map { track ->
                            if (track.id == videoTrack.id) {
                                track.copy(items = track.items + newItem)
                            } else {
                                track
                            }
                        }
                        
                        val updatedProject = project.copy(
                            tracks = updatedTracks,
                            duration = project.duration + videoInfo.duration,
                            updatedAt = System.currentTimeMillis()
                        )
                        
                        videoProjectDao.updateProject(updatedProject)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

enum class CameraFacing {
    FRONT, BACK
}