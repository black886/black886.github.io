package com.cutwa.videoeditor.ui.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cutwa.videoeditor.data.model.VideoProject
import com.cutwa.videoeditor.ui.editor.components.*
import com.cutwa.videoeditor.ui.theme.CutwaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoEditorActivity : ComponentActivity() {

    private val viewModel: VideoEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val project = intent.getParcelableExtra<VideoProject>("project")
        if (project != null) {
            viewModel.loadProject(project)
        }

        setContent {
            CutwaTheme {
                VideoEditorScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    viewModel: VideoEditorViewModel,
    onNavigateBack: () -> Unit
) {
    val project by viewModel.currentProject.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
    
    var showExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        project?.name ?: "Video Editor",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Undo */ }) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = { /* Redo */ }) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo")
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Video Preview Area
            VideoPreviewArea(
                project = project,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                onPlayPause = { viewModel.togglePlayback() },
                onSeek = { position -> viewModel.seekTo(position) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Tools Panel
            ToolsPanel(
                selectedTool = selectedTool,
                onToolSelected = { tool -> viewModel.selectTool(tool) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            // Timeline
            TimelineView(
                project = project,
                currentPosition = currentPosition,
                onItemSelected = { item -> viewModel.selectTimelineItem(item) },
                onItemMoved = { item, newPosition -> viewModel.moveTimelineItem(item, newPosition) },
                onItemTrimmed = { item, start, end -> viewModel.trimTimelineItem(item, start, end) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            // Playback Controls
            PlaybackControls(
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = project?.duration ?: 0L,
                onPlayPause = { viewModel.togglePlayback() },
                onSeek = { position -> viewModel.seekTo(position) },
                onPrevious = { /* Previous frame */ },
                onNext = { /* Next frame */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { settings ->
                viewModel.exportVideo(settings)
                showExportDialog = false
            }
        )
    }
}

@Composable
fun VideoPreviewArea(
    project: VideoProject?,
    isPlaying: Boolean,
    currentPosition: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black, RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (project != null) {
            // Video preview would go here
            // Using ExoPlayer or similar video player
            VideoPlayerView(
                project = project,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                onSeek = onSeek,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.VideoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Text(
                    "No video loaded",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Play/Pause overlay
        FloatingActionButton(
            onClick = onPlayPause,
            modifier = Modifier.align(Alignment.Center),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White
            )
        }
    }
}

@Composable
fun ToolsPanel(
    selectedTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton(
                icon = Icons.Default.ContentCut,
                label = "Cut",
                isSelected = selectedTool == EditorTool.CUT,
                onClick = { onToolSelected(EditorTool.CUT) }
            )
            ToolButton(
                icon = Icons.Default.FilterList,
                label = "Filters",
                isSelected = selectedTool == EditorTool.FILTERS,
                onClick = { onToolSelected(EditorTool.FILTERS) }
            )
            ToolButton(
                icon = Icons.Default.TextFields,
                label = "Text",
                isSelected = selectedTool == EditorTool.TEXT,
                onClick = { onToolSelected(EditorTool.TEXT) }
            )
            ToolButton(
                icon = Icons.Default.EmojiEmotions,
                label = "Stickers",
                isSelected = selectedTool == EditorTool.STICKERS,
                onClick = { onToolSelected(EditorTool.STICKERS) }
            )
            ToolButton(
                icon = Icons.Default.Speed,
                label = "Speed",
                isSelected = selectedTool == EditorTool.SPEED,
                onClick = { onToolSelected(EditorTool.SPEED) }
            )
            ToolButton(
                icon = Icons.Default.MusicNote,
                label = "Audio",
                isSelected = selectedTool == EditorTool.AUDIO,
                onClick = { onToolSelected(EditorTool.AUDIO) }
            )
        }
    }
}

@Composable
fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }

            IconButton(onClick = onPlayPause) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }

            IconButton(onClick = onNext) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }

            Slider(
                value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                onValueChange = { progress ->
                    val newPosition = (progress * duration).toLong()
                    onSeek(newPosition)
                },
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (ExportSettings) -> Unit
) {
    var selectedResolution by remember { mutableStateOf("1920x1080") }
    var selectedQuality by remember { mutableStateOf("High") }
    var selectedFormat by remember { mutableStateOf("MP4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Video") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Resolution selection
                Text("Resolution")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("720x1280", "1080x1920", "1920x1080").forEach { resolution ->
                        FilterChip(
                            selected = selectedResolution == resolution,
                            onClick = { selectedResolution = resolution },
                            label = { Text(resolution) }
                        )
                    }
                }

                // Quality selection
                Text("Quality")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Low", "Medium", "High").forEach { quality ->
                        FilterChip(
                            selected = selectedQuality == quality,
                            onClick = { selectedQuality = quality },
                            label = { Text(quality) }
                        )
                    }
                }

                // Format selection
                Text("Format")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("MP4", "MOV", "AVI").forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val settings = ExportSettings(
                        resolution = selectedResolution,
                        quality = selectedQuality,
                        format = selectedFormat
                    )
                    onExport(settings)
                }
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class ExportSettings(
    val resolution: String,
    val quality: String,
    val format: String
)

enum class EditorTool {
    CUT, FILTERS, TEXT, STICKERS, SPEED, AUDIO, TRANSITIONS, EFFECTS
}

fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}