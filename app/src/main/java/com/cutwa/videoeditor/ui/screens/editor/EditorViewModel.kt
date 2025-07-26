package com.cutwa.videoeditor.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutwa.videoeditor.data.model.*
import com.cutwa.videoeditor.data.repository.VideoProjectRepository
import com.cutwa.videoeditor.engine.VideoProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val projectRepository: VideoProjectRepository,
    private val videoProcessor: VideoProcessor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()
    
    private val undoStack = mutableListOf<VideoProject>()
    private val redoStack = mutableListOf<VideoProject>()
    
    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val project = projectRepository.getProjectById(projectId)
            project?.let {
                _uiState.value = _uiState.value.copy(project = it)
                undoStack.clear()
                redoStack.clear()
            }
        }
    }
    
    fun togglePlayback() {
        _uiState.value = _uiState.value.copy(
            isPlaying = !_uiState.value.isPlaying
        )
    }
    
    fun seekTo(time: Long) {
        _uiState.value = _uiState.value.copy(
            currentTime = time,
            isPlaying = false
        )
    }
    
    fun selectClip(clip: TimelineClip?) {
        _uiState.value = _uiState.value.copy(selectedClip = clip)
    }
    
    fun selectTool(tool: EditorTool) {
        _uiState.value = _uiState.value.copy(selectedTool = tool)
    }
    
    fun moveClip(clipId: String, newTime: Long) {
        val currentProject = _uiState.value.project ?: return
        saveStateForUndo(currentProject)
        
        val updatedTracks = currentProject.tracks.map { track ->
            track.copy(
                clips = track.clips.map { clip ->
                    if (clip.id == clipId) {
                        val duration = clip.endTime - clip.startTime
                        when (clip) {
                            is TimelineClip.VideoClip -> clip.copy(
                                startTime = newTime,
                                endTime = newTime + duration
                            )
                            is TimelineClip.AudioClip -> clip.copy(
                                startTime = newTime,
                                endTime = newTime + duration
                            )
                            is TimelineClip.TextClip -> clip.copy(
                                startTime = newTime,
                                endTime = newTime + duration
                            )
                            is TimelineClip.ImageClip -> clip.copy(
                                startTime = newTime,
                                endTime = newTime + duration
                            )
                        }
                    } else clip
                }
            )
        }
        
        val updatedProject = currentProject.copy(
            tracks = updatedTracks,
            lastModified = System.currentTimeMillis()
        )
        
        updateProject(updatedProject)
    }
    
    fun trimClip(clipId: String, startTime: Long, endTime: Long) {
        val currentProject = _uiState.value.project ?: return
        saveStateForUndo(currentProject)
        
        val updatedTracks = currentProject.tracks.map { track ->
            track.copy(
                clips = track.clips.map { clip ->
                    if (clip.id == clipId) {
                        when (clip) {
                            is TimelineClip.VideoClip -> clip.copy(
                                startTime = startTime,
                                endTime = endTime
                            )
                            is TimelineClip.AudioClip -> clip.copy(
                                startTime = startTime,
                                endTime = endTime
                            )
                            is TimelineClip.TextClip -> clip.copy(
                                startTime = startTime,
                                endTime = endTime
                            )
                            is TimelineClip.ImageClip -> clip.copy(
                                startTime = startTime,
                                endTime = endTime
                            )
                        }
                    } else clip
                }
            )
        }
        
        val updatedProject = currentProject.copy(
            tracks = updatedTracks,
            lastModified = System.currentTimeMillis()
        )
        
        updateProject(updatedProject)
    }
    
    fun splitClip(clipId: String, time: Long) {
        val currentProject = _uiState.value.project ?: return
        saveStateForUndo(currentProject)
        
        val updatedTracks = currentProject.tracks.map { track ->
            val updatedClips = mutableListOf<TimelineClip>()
            
            track.clips.forEach { clip ->
                if (clip.id == clipId && time > clip.startTime && time < clip.endTime) {
                    // Split the clip into two parts
                    val firstClip = when (clip) {
                        is TimelineClip.VideoClip -> clip.copy(
                            id = UUID.randomUUID().toString(),
                            endTime = time
                        )
                        is TimelineClip.AudioClip -> clip.copy(
                            id = UUID.randomUUID().toString(),
                            endTime = time
                        )
                        is TimelineClip.TextClip -> clip.copy(
                            id = UUID.randomUUID().toString(),
                            endTime = time
                        )
                        is TimelineClip.ImageClip -> clip.copy(
                            id = UUID.randomUUID().toString(),
                            endTime = time
                        )
                    }
                    
                    val secondClip = when (clip) {
                        is TimelineClip.VideoClip -> clip.copy(
                            id = UUID.randomUUID().toString(),
                            startTime = time
                        )
                        is TimelineClip.AudioClip -> clip.copy(
                            id = UUID.randomUUID().toString(),
                            startTime = time
                        )
                        is TimelineClip.TextClip -> clip.copy(
                            id = UUID.randomUUID().toString(),
                            startTime = time
                        )
                        is TimelineClip.ImageClip -> clip.copy(
                            id = UUID.randomUUID().toString(),
                            startTime = time
                        )
                    }
                    
                    updatedClips.add(firstClip)
                    updatedClips.add(secondClip)
                } else {
                    updatedClips.add(clip)
                }
            }
            
            track.copy(clips = updatedClips)
        }
        
        val updatedProject = currentProject.copy(
            tracks = updatedTracks,
            lastModified = System.currentTimeMillis()
        )
        
        updateProject(updatedProject)
    }
    
    fun applyEffect(effect: Effect) {
        val currentProject = _uiState.value.project ?: return
        val selectedClip = _uiState.value.selectedClip ?: return
        saveStateForUndo(currentProject)
        
        val updatedTracks = currentProject.tracks.map { track ->
            track.copy(
                clips = track.clips.map { clip ->
                    if (clip.id == selectedClip.id && clip is TimelineClip.VideoClip) {
                        clip.copy(effects = clip.effects + effect)
                    } else clip
                }
            )
        }
        
        val updatedProject = currentProject.copy(
            tracks = updatedTracks,
            lastModified = System.currentTimeMillis()
        )
        
        updateProject(updatedProject)
    }
    
    fun applyFilter(filter: Filter) {
        val currentProject = _uiState.value.project ?: return
        val selectedClip = _uiState.value.selectedClip ?: return
        saveStateForUndo(currentProject)
        
        val updatedTracks = currentProject.tracks.map { track ->
            track.copy(
                clips = track.clips.map { clip ->
                    if (clip.id == selectedClip.id && clip is TimelineClip.VideoClip) {
                        clip.copy(filters = clip.filters + filter)
                    } else clip
                }
            )
        }
        
        val updatedProject = currentProject.copy(
            tracks = updatedTracks,
            lastModified = System.currentTimeMillis()
        )
        
        updateProject(updatedProject)
    }
    
    fun addText(text: String) {
        val currentProject = _uiState.value.project ?: return
        saveStateForUndo(currentProject)
        
        val textClip = TimelineClip.TextClip(
            id = UUID.randomUUID().toString(),
            startTime = _uiState.value.currentTime,
            endTime = _uiState.value.currentTime + 5000, // 5 seconds default
            trackId = "text_track_1",
            text = text,
            fontFamily = "Arial",
            fontSize = 24f,
            color = "#FFFFFF",
            position = Position(0.5f, 0.5f) // Center position
        )
        
        val updatedTracks = currentProject.tracks.map { track ->
            if (track.type == TrackType.TEXT) {
                track.copy(clips = track.clips + textClip)
            } else track
        }
        
        val updatedProject = currentProject.copy(
            tracks = updatedTracks,
            lastModified = System.currentTimeMillis()
        )
        
        updateProject(updatedProject)
    }
    
    fun addSticker(sticker: String) {
        val currentProject = _uiState.value.project ?: return
        saveStateForUndo(currentProject)
        
        val stickerClip = TimelineClip.ImageClip(
            id = UUID.randomUUID().toString(),
            startTime = _uiState.value.currentTime,
            endTime = _uiState.value.currentTime + 3000, // 3 seconds default
            trackId = "overlay_track_1",
            filePath = sticker,
            position = Position(0.5f, 0.5f),
            scale = 1.0f,
            rotation = 0.0f
        )
        
        val updatedTracks = currentProject.tracks.map { track ->
            if (track.type == TrackType.OVERLAY) {
                track.copy(clips = track.clips + stickerClip)
            } else track
        }
        
        val updatedProject = currentProject.copy(
            tracks = updatedTracks,
            lastModified = System.currentTimeMillis()
        )
        
        updateProject(updatedProject)
    }
    
    fun showMediaPicker(trackId: String) {
        // This would trigger a media picker dialog
        // Implementation depends on the UI framework
    }
    
    fun changePlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }
    
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentProject = _uiState.value.project
            currentProject?.let { redoStack.add(it) }
            
            val previousProject = undoStack.removeLastOrNull()
            previousProject?.let {
                _uiState.value = _uiState.value.copy(
                    project = it,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }
    
    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentProject = _uiState.value.project
            currentProject?.let { undoStack.add(it) }
            
            val nextProject = redoStack.removeLastOrNull()
            nextProject?.let {
                _uiState.value = _uiState.value.copy(
                    project = it,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
        }
    }
    
    fun exportVideo() {
        val project = _uiState.value.project ?: return
        
        _uiState.value = _uiState.value.copy(isExporting = true, exportProgress = 0f)
        
        viewModelScope.launch {
            try {
                val outputPath = "/storage/emulated/0/Movies/cutwa_export_${System.currentTimeMillis()}.mp4"
                
                videoProcessor.processVideo(
                    project = project,
                    outputPath = outputPath,
                    onProgress = { progress ->
                        _uiState.value = _uiState.value.copy(
                            exportProgress = progress.percentage / 100f
                        )
                    }
                ).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            exportProgress = 0f
                        )
                        // Show success message
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            exportProgress = 0f,
                            error = "Export failed: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportProgress = 0f,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }
    
    fun cancelExport() {
        // Cancel the export process
        _uiState.value = _uiState.value.copy(
            isExporting = false,
            exportProgress = 0f
        )
    }
    
    private fun saveStateForUndo(project: VideoProject) {
        undoStack.add(project)
        if (undoStack.size > 50) { // Limit undo history
            undoStack.removeFirstOrNull()
        }
        redoStack.clear()
        
        _uiState.value = _uiState.value.copy(
            canUndo = true,
            canRedo = false
        )
    }
    
    private fun updateProject(project: VideoProject) {
        _uiState.value = _uiState.value.copy(project = project)
        
        viewModelScope.launch {
            projectRepository.updateProject(project)
        }
    }
}

data class EditorUiState(
    val project: VideoProject? = null,
    val currentTime: Long = 0,
    val isPlaying: Boolean = false,
    val selectedClip: TimelineClip? = null,
    val selectedTool: EditorTool = EditorTool.SELECT,
    val playbackSpeed: Float = 1.0f,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val error: String? = null
)

enum class EditorTool {
    SELECT,
    CUT,
    TEXT,
    EFFECTS,
    FILTERS,
    AUDIO,
    STICKERS,
    TRANSITIONS
}