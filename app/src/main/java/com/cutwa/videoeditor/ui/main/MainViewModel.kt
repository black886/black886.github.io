package com.cutwa.videoeditor.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutwa.videoeditor.data.database.VideoProjectDao
import com.cutwa.videoeditor.data.model.*
import com.cutwa.videoeditor.util.VideoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoProjectDao: VideoProjectDao,
    private val videoUtils: VideoUtils
) : ViewModel() {

    private val _projects = MutableStateFlow<List<VideoProject>>(emptyList())
    val projects: StateFlow<List<VideoProject>> = _projects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            videoProjectDao.getAllProjects().collect { projectList ->
                _projects.value = projectList
            }
        }
    }

    fun createProjectFromVideo(videoUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val videoInfo = videoUtils.getVideoInfo(videoUri)
                val projectId = UUID.randomUUID().toString()
                
                val project = VideoProject(
                    id = projectId,
                    name = "Video Project ${System.currentTimeMillis()}",
                    thumbnailPath = videoUtils.generateThumbnail(videoUri, projectId),
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
                                    sourceUri = videoUri.toString()
                                )
                            )
                        )
                    ),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                videoProjectDao.insertProject(project)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProject(project: VideoProject) {
        viewModelScope.launch {
            try {
                videoProjectDao.deleteProject(project)
                // Also delete associated files
                videoUtils.deleteProjectFiles(project.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createNewProject(
        name: String,
        aspectRatio: AspectRatio,
        resolution: Resolution,
        frameRate: Int = 30
    ) {
        viewModelScope.launch {
            try {
                val projectId = UUID.randomUUID().toString()
                val project = VideoProject(
                    id = projectId,
                    name = name,
                    thumbnailPath = "",
                    duration = 0L,
                    aspectRatio = aspectRatio,
                    resolution = resolution,
                    frameRate = frameRate,
                    tracks = emptyList(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                videoProjectDao.insertProject(project)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}