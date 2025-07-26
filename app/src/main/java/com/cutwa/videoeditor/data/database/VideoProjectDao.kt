package com.cutwa.videoeditor.data.database

import androidx.room.*
import com.cutwa.videoeditor.data.model.VideoProject
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoProjectDao {
    
    @Query("SELECT * FROM video_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<VideoProject>>
    
    @Query("SELECT * FROM video_projects WHERE id = :id")
    suspend fun getProject(id: String): VideoProject?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: VideoProject)
    
    @Update
    suspend fun updateProject(project: VideoProject)
    
    @Delete
    suspend fun deleteProject(project: VideoProject)
    
    @Query("DELETE FROM video_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
    
    @Query("SELECT COUNT(*) FROM video_projects")
    suspend fun getProjectCount(): Int
}