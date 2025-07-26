package com.cutwa.videoeditor.data.database

import androidx.room.*
import com.cutwa.videoeditor.data.model.VideoProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    
    @Query("SELECT * FROM video_projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<VideoProject>>
    
    @Query("SELECT * FROM video_projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: String): VideoProject?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: VideoProject)
    
    @Update
    suspend fun updateProject(project: VideoProject)
    
    @Delete
    suspend fun deleteProject(project: VideoProject)
    
    @Query("DELETE FROM video_projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)
    
    @Query("SELECT COUNT(*) FROM video_projects")
    suspend fun getProjectCount(): Int
    
    @Query("SELECT * FROM video_projects WHERE name LIKE '%' || :searchQuery || '%' ORDER BY lastModified DESC")
    fun searchProjects(searchQuery: String): Flow<List<VideoProject>>
}

@Database(
    entities = [VideoProject::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(com.cutwa.videoeditor.data.model.Converters::class)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}