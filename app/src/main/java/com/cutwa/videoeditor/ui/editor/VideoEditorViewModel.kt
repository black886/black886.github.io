package com.cutwa.videoeditor.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutwa.videoeditor.data.database.VideoProjectDao
import com.cutwa.videoeditor.data.model.*
import com.cutwa.videoeditor.util.VideoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoEditorViewModel @Inject constructor(
    private val videoProjectDao: VideoProjectDao,
    private val videoUtils: VideoUtils
) : ViewModel() {

    private val _currentProject = MutableStateFlow<VideoProject?>(null)
    val currentProject: StateFlow<VideoProject?> = _currentProject.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _selectedTool = MutableStateFlow(EditorTool.CUT)
    val selectedTool: StateFlow<EditorTool> = _selectedTool.asStateFlow()

    private val _selectedTrackItem = MutableStateFlow<TrackItem?>(null)
    val selectedTrackItem: StateFlow<TrackItem?> = _selectedTrackItem.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    fun loadProject(project: VideoProject) {
        _currentProject.value = project
    }

    fun togglePlayback() {
        _isPlaying.value = !_isPlaying.value
    }

    fun seekTo(position: Long) {
        _currentPosition.value = position
    }

    fun selectTool(tool: EditorTool) {
        _selectedTool.value = tool
    }

    fun selectTimelineItem(item: TrackItem) {
        _selectedTrackItem.value = item
    }

    fun moveTimelineItem(item: TrackItem, newPosition: Long) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            track.copy(
                items = track.items.map { trackItem ->
                    if (trackItem.id == item.id) {
                        trackItem.copy(startTime = newPosition)
                    } else {
                        trackItem
                    }
                }
            )
        }
        updateProject(project.copy(tracks = updatedTracks))
    }

    fun trimTimelineItem(item: TrackItem, trimStart: Long, trimEnd: Long) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            track.copy(
                items = track.items.map { trackItem ->
                    if (trackItem.id == item.id) {
                        trackItem.copy(
                            trimStart = trimStart,
                            trimEnd = trimEnd,
                            duration = item.duration - trimStart - trimEnd
                        )
                    } else {
                        trackItem
                    }
                }
            )
        }
        updateProject(project.copy(tracks = updatedTracks))
    }

    fun splitClip(item: TrackItem, splitTime: Long) {
        val project = _currentProject.value ?: return
        val newItemId = java.util.UUID.randomUUID().toString()
        
        val firstPart = item.copy(
            duration = splitTime - item.startTime,
            trimEnd = item.trimEnd + (item.duration - (splitTime - item.startTime))
        )
        
        val secondPart = item.copy(
            id = newItemId,
            startTime = splitTime,
            duration = item.duration - (splitTime - item.startTime),
            trimStart = item.trimStart + (splitTime - item.startTime)
        )

        val updatedTracks = project.tracks.map { track ->
            track.copy(
                items = track.items.flatMap { trackItem ->
                    if (trackItem.id == item.id) {
                        listOf(firstPart, secondPart)
                    } else {
                        listOf(trackItem)
                    }
                }
            )
        }
        updateProject(project.copy(tracks = updatedTracks))
    }

    fun addFilter(item: TrackItem, filter: Filter) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            track.copy(
                items = track.items.map { trackItem ->
                    if (trackItem.id == item.id) {
                        trackItem.copy(filters = trackItem.filters + filter)
                    } else {
                        trackItem
                    }
                }
            )
        }
        updateProject(project.copy(tracks = updatedTracks))
    }

    fun removeFilter(item: TrackItem, filterId: String) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            track.copy(
                items = track.items.map { trackItem ->
                    if (trackItem.id == item.id) {
                        trackItem.copy(filters = trackItem.filters.filter { it.id != filterId })
                    } else {
                        trackItem
                    }
                }
            )
        }
        updateProject(project.copy(tracks = updatedTracks))
    }

    fun addEffect(item: TrackItem, effect: Effect) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            track.copy(
                items = track.items.map { trackItem ->
                    if (trackItem.id == item.id) {
                        trackItem.copy(effects = trackItem.effects + effect)
                    } else {
                        trackItem
                    }
                }
            )
        }
        updateProject(project.copy(tracks = updatedTracks))
    }

    fun changeSpeed(item: TrackItem, speed: Float) {
        val project = _currentProject.value ?: return
        val updatedTracks = project.tracks.map { track ->
            track.copy(
                items = track.items.map { trackItem ->
                    if (trackItem.id == item.id) {
                        trackItem.copy(
                            speed = speed,
                            duration = (trackItem.duration / speed).toLong()
                        )
                    } else {
                        trackItem
                    }
                }
            )
        }
        updateProject(project.copy(tracks = updatedTracks))
    }

    fun addTextTrackItem(text: String, startTime: Long, duration: Long) {
        val project = _currentProject.value ?: return
        val textItem = TrackItem(
            id = java.util.UUID.randomUUID().toString(),
            type = TrackItemType.TEXT,
            startTime = startTime,
            duration = duration,
            sourceUri = text
        )

        val textTrack = project.tracks.find { it.type == TrackType.TEXT }
        if (textTrack != null) {
            val updatedTracks = project.tracks.map { track ->
                if (track.id == textTrack.id) {
                    track.copy(items = track.items + textItem)
                } else {
                    track
                }
            }
            updateProject(project.copy(tracks = updatedTracks))
        } else {
            val newTextTrack = Track(
                id = java.util.UUID.randomUUID().toString(),
                type = TrackType.TEXT,
                items = listOf(textItem)
            )
            updateProject(project.copy(tracks = project.tracks + newTextTrack))
        }
    }

    fun addAudioTrack(audioUri: String, startTime: Long) {
        viewModelScope.launch {
            try {
                val project = _currentProject.value ?: return@launch
                val audioInfo = videoUtils.getVideoInfo(android.net.Uri.parse(audioUri))
                
                val audioItem = TrackItem(
                    id = java.util.UUID.randomUUID().toString(),
                    type = TrackItemType.AUDIO_CLIP,
                    startTime = startTime,
                    duration = audioInfo.duration,
                    sourceUri = audioUri
                )

                val audioTrack = project.tracks.find { it.type == TrackType.AUDIO }
                if (audioTrack != null) {
                    val updatedTracks = project.tracks.map { track ->
                        if (track.id == audioTrack.id) {
                            track.copy(items = track.items + audioItem)
                        } else {
                            track
                        }
                    }
                    updateProject(project.copy(tracks = updatedTracks))
                } else {
                    val newAudioTrack = Track(
                        id = java.util.UUID.randomUUID().toString(),
                        type = TrackType.AUDIO,
                        items = listOf(audioItem)
                    )
                    updateProject(project.copy(tracks = project.tracks + newAudioTrack))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun exportVideo(settings: ExportSettings) {
        val project = _currentProject.value ?: return
        
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _exportProgress.value = 0f
                
                val outputPath = videoUtils.getProjectDir(project.id).resolve("export_${System.currentTimeMillis()}.${settings.format.lowercase()}")
                
                // This is a simplified export process
                // In a real implementation, you would process all tracks, apply filters, effects, etc.
                val success = videoUtils.exportVideo(
                    inputPath = project.tracks.firstOrNull()?.items?.firstOrNull()?.sourceUri ?: "",
                    outputPath = outputPath.absolutePath,
                    resolution = settings.resolution,
                    bitrate = when (settings.quality) {
                        "Low" -> "2000k"
                        "Medium" -> "5000k"
                        "High" -> "8000k"
                        else -> "5000k"
                    },
                    onProgress = { progress -> _exportProgress.value = progress }
                )
                
                if (success) {
                    updateProject(project.copy(exportPath = outputPath.absolutePath))
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isExporting.value = false
                _exportProgress.value = 0f
            }
        }
    }

    private fun updateProject(project: VideoProject) {
        val updatedProject = project.copy(updatedAt = System.currentTimeMillis())
        _currentProject.value = updatedProject
        
        viewModelScope.launch {
            try {
                videoProjectDao.updateProject(updatedProject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun undo() {
        // Implement undo functionality
        // This would require maintaining a history of project states
    }

    fun redo() {
        // Implement redo functionality
        // This would require maintaining a history of project states
    }
}