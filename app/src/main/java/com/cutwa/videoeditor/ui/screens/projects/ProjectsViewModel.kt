package com.cutwa.videoeditor.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cutwa.videoeditor.data.model.VideoProject
import com.cutwa.videoeditor.data.repository.VideoProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: VideoProjectRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    
    val uiState: StateFlow<ProjectsUiState> = combine(
        searchQuery,
        _isLoading
    ) { query, loading ->
        ProjectsUiState(
            searchQuery = query,
            isLoading = loading,
            projects = emptyList() // Will be updated by projects flow
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProjectsUiState()
    )
    
    private val _projects = MutableStateFlow<List<VideoProject>>(emptyList())
    
    init {
        loadProjects()
    }
    
    private fun loadProjects() {
        viewModelScope.launch {
            combine(
                projectRepository.getAllProjects(),
                searchQuery
            ) { projects, query ->
                if (query.isBlank()) {
                    projects
                } else {
                    projects.filter { project ->
                        project.name.contains(query, ignoreCase = true)
                    }
                }
            }.collect { filteredProjects ->
                _projects.value = filteredProjects
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.deleteProjectById(projectId)
            } catch (e: Exception) {
                // Handle error - could show snackbar or toast
            }
        }
    }
}

data class ProjectsUiState(
    val projects: List<VideoProject> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)