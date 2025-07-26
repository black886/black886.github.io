package com.cutwa.videoeditor.data.repository

import com.cutwa.videoeditor.data.database.ProjectDao
import com.cutwa.videoeditor.data.model.VideoProject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    
    fun getAllProjects(): Flow<List<VideoProject>> = projectDao.getAllProjects()
    
    suspend fun getProjectById(projectId: String): VideoProject? = projectDao.getProjectById(projectId)
    
    suspend fun createProject(project: VideoProject) = projectDao.insertProject(project)
    
    suspend fun updateProject(project: VideoProject) = projectDao.updateProject(project)
    
    suspend fun deleteProject(project: VideoProject) = projectDao.deleteProject(project)
    
    suspend fun deleteProjectById(projectId: String) = projectDao.deleteProjectById(projectId)
    
    suspend fun getProjectCount(): Int = projectDao.getProjectCount()
    
    fun searchProjects(query: String): Flow<List<VideoProject>> = projectDao.searchProjects(query)
}