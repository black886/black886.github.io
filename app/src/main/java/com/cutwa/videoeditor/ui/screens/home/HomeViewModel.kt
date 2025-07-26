package com.cutwa.videoeditor.ui.screens.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutwa.videoeditor.data.model.*
import com.cutwa.videoeditor.data.repository.VideoProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val projectRepository: VideoProjectRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadRecentProjects()
    }
    
    private fun loadRecentProjects() {
        viewModelScope.launch {
            projectRepository.getAllProjects().collect { projects ->
                _uiState.value = _uiState.value.copy(
                    recentProjects = projects.take(5)
                )
            }
        }
    }
    
    fun createEmptyProject() {
        viewModelScope.launch {
            val projectId = UUID.randomUUID().toString()
            val project = VideoProject(
                id = projectId,
                name = "New Project",
                createdAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis(),
                thumbnailPath = null,
                duration = 0,
                aspectRatio = AspectRatio.RATIO_16_9,
                resolution = Resolution(1920, 1080),
                frameRate = 30,
                tracks = createDefaultTracks()
            )
            
            projectRepository.createProject(project)
            _uiState.value = _uiState.value.copy(newProjectId = projectId)
        }
    }
    
    fun createProjectFromVideo(videoUri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val projectId = UUID.randomUUID().toString()
                
                // Get video file path from URI
                val videoPath = getPathFromUri(videoUri, context) ?: return@launch
                
                // Create video clip
                val videoClip = TimelineClip.VideoClip(
                    id = UUID.randomUUID().toString(),
                    startTime = 0,
                    endTime = 30000, // Default 30 seconds, should be detected from video
                    trackId = "video_track_1",
                    filePath = videoPath
                )
                
                val videoTrack = Track(
                    id = "video_track_1",
                    type = TrackType.VIDEO,
                    name = "Video 1",
                    clips = listOf(videoClip)
                )
                
                val project = VideoProject(
                    id = projectId,
                    name = "Imported Video",
                    createdAt = System.currentTimeMillis(),
                    lastModified = System.currentTimeMillis(),
                    thumbnailPath = null,
                    duration = 30000,
                    aspectRatio = AspectRatio.RATIO_16_9,
                    resolution = Resolution(1920, 1080),
                    frameRate = 30,
                    tracks = listOf(videoTrack) + createDefaultTracks().drop(1)
                )
                
                projectRepository.createProject(project)
                _uiState.value = _uiState.value.copy(newProjectId = projectId)
            } catch (e: Exception) {
                // Handle error
                _uiState.value = _uiState.value.copy(
                    error = "Failed to import video: ${e.message}"
                )
            }
        }
    }
    
    fun createProjectFromTemplate(template: TemplateOption) {
        viewModelScope.launch {
            val projectId = UUID.randomUUID().toString()
            val resolution = when (template.aspectRatio) {
                AspectRatio.RATIO_16_9 -> Resolution(1920, 1080)
                AspectRatio.RATIO_9_16 -> Resolution(1080, 1920)
                AspectRatio.RATIO_1_1 -> Resolution(1080, 1080)
                AspectRatio.RATIO_4_3 -> Resolution(1440, 1080)
                AspectRatio.RATIO_21_9 -> Resolution(2560, 1080)
            }
            
            val project = VideoProject(
                id = projectId,
                name = "${template.title} Project",
                createdAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis(),
                thumbnailPath = null,
                duration = 0,
                aspectRatio = template.aspectRatio,
                resolution = resolution,
                frameRate = 30,
                tracks = createDefaultTracks()
            )
            
            projectRepository.createProject(project)
            _uiState.value = _uiState.value.copy(newProjectId = projectId)
        }
    }
    
    fun startRecording(context: Context) {
        // TODO: Implement camera recording functionality
        // This would open the camera and start recording
        viewModelScope.launch {
            // For now, create an empty project
            createEmptyProject()
        }
    }
    
    fun onProjectOpened() {
        _uiState.value = _uiState.value.copy(newProjectId = null)
    }
    
    private fun createDefaultTracks(): List<Track> {
        return listOf(
            Track(
                id = "video_track_1",
                type = TrackType.VIDEO,
                name = "Video 1"
            ),
            Track(
                id = "audio_track_1",
                type = TrackType.AUDIO,
                name = "Audio 1"
            ),
            Track(
                id = "text_track_1",
                type = TrackType.TEXT,
                name = "Text 1"
            ),
            Track(
                id = "overlay_track_1",
                type = TrackType.OVERLAY,
                name = "Overlay 1"
            )
        )
    }
    
    private fun getPathFromUri(uri: Uri, context: Context): String? {
        // This is a simplified implementation
        // In a real app, you would need to handle different URI schemes
        // and copy the file to app storage
        return uri.path
    }
}

data class HomeUiState(
    val recentProjects: List<VideoProject> = emptyList(),
    val newProjectId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)