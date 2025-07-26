package com.cutwa.videoeditor.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cutwa.videoeditor.ui.components.timeline.TimelineView
import com.cutwa.videoeditor.ui.components.preview.VideoPreview
import com.cutwa.videoeditor.ui.components.tools.ToolsPanel
import com.cutwa.videoeditor.ui.theme.*

@Composable
fun EditorScreen(
    projectId: String?,
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(projectId) {
        projectId?.let { viewModel.loadProject(it) }
    }
    
    if (uiState.project == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // Top App Bar
        EditorTopBar(
            projectName = uiState.project!!.name,
            onNavigateBack = onNavigateBack,
            onExport = { viewModel.exportVideo() },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            canUndo = uiState.canUndo,
            canRedo = uiState.canRedo
        )
        
        Row(modifier = Modifier.weight(1f)) {
            // Main editing area
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Video Preview
                VideoPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            uiState.project!!.resolution.width.toFloat() / 
                            uiState.project!!.resolution.height.toFloat()
                        ),
                    project = uiState.project!!,
                    currentTime = uiState.currentTime,
                    isPlaying = uiState.isPlaying,
                    onPlayPause = { viewModel.togglePlayback() },
                    onSeek = { time -> viewModel.seekTo(time) }
                )
                
                // Timeline
                TimelineView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    project = uiState.project!!,
                    currentTime = uiState.currentTime,
                    selectedClip = uiState.selectedClip,
                    onSeek = { time -> viewModel.seekTo(time) },
                    onClipSelected = { clip -> viewModel.selectClip(clip) },
                    onClipMoved = { clipId, newTime -> viewModel.moveClip(clipId, newTime) },
                    onClipTrimmed = { clipId, startTime, endTime -> 
                        viewModel.trimClip(clipId, startTime, endTime) 
                    },
                    onClipSplit = { clipId, time -> viewModel.splitClip(clipId, time) },
                    onAddClip = { trackId -> viewModel.showMediaPicker(trackId) }
                )
            }
            
            // Tools Panel
            ToolsPanel(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight(),
                selectedTool = uiState.selectedTool,
                selectedClip = uiState.selectedClip,
                onToolSelected = { tool -> viewModel.selectTool(tool) },
                onEffectApplied = { effect -> viewModel.applyEffect(effect) },
                onFilterApplied = { filter -> viewModel.applyFilter(filter) },
                onTextAdded = { text -> viewModel.addText(text) },
                onStickerAdded = { sticker -> viewModel.addSticker(sticker) }
            )
        }
        
        // Bottom Controls
        EditorBottomBar(
            isPlaying = uiState.isPlaying,
            currentTime = uiState.currentTime,
            duration = uiState.project!!.duration,
            onPlayPause = { viewModel.togglePlayback() },
            onSeek = { time -> viewModel.seekTo(time) },
            onSpeedChange = { speed -> viewModel.changePlaybackSpeed(speed) },
            playbackSpeed = uiState.playbackSpeed
        )
    }
    
    // Handle export progress
    if (uiState.isExporting) {
        ExportProgressDialog(
            progress = uiState.exportProgress,
            onCancel = { viewModel.cancelExport() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    projectName: String,
    onNavigateBack: () -> Unit,
    onExport: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean
) {
    TopAppBar(
        title = { Text(text = projectName, color = White) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }
        },
        actions = {
            IconButton(
                onClick = onUndo,
                enabled = canUndo
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) White else White.copy(alpha = 0.3f)
                )
            }
            
            IconButton(
                onClick = onRedo,
                enabled = canRedo
            ) {
                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) White else White.copy(alpha = 0.3f)
                )
            }
            
            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export",
                    tint = White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Export", color = White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ToolBackground
        )
    )
}

@Composable
private fun EditorBottomBar(
    isPlaying: Boolean,
    currentTime: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    playbackSpeed: Float
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = ToolBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause button
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Time display
            Text(
                text = formatTime(currentTime),
                color = White
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Seek bar
            Slider(
                value = currentTime.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = AccentBlue,
                    activeTrackColor = AccentBlue,
                    inactiveTrackColor = White.copy(alpha = 0.3f)
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = formatTime(duration),
                color = White
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Speed control
            Text(
                text = "${playbackSpeed}x",
                color = White
            )
        }
    }
}

@Composable
private fun ExportProgressDialog(
    progress: Float,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Exporting Video") },
        text = {
            Column {
                Text("Please wait while your video is being processed...")
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("${(progress * 100).toInt()}%")
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}