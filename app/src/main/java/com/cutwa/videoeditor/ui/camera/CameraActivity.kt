package com.cutwa.videoeditor.ui.camera

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cutwa.videoeditor.ui.editor.VideoEditorActivity
import com.cutwa.videoeditor.ui.theme.CutwaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraActivity : ComponentActivity() {

    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CutwaTheme {
                CameraScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    onNavigateToEditor = { projectId ->
                        val intent = Intent(this, VideoEditorActivity::class.java)
                        intent.putExtra("projectId", projectId)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String) -> Unit
) {
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.recordingDuration.collectAsStateWithLifecycle()
    val cameraFacing by viewModel.cameraFacing.collectAsStateWithLifecycle()
    val flashMode by viewModel.flashMode.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onCameraReady = { /* Handle camera ready */ }
        )
        
        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            // Recording duration
            if (isRecording) {
                Card(
                    modifier = Modifier.background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, CircleShape)
                        )
                        Text(
                            text = formatRecordingTime(recordingDuration),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Flash toggle
            IconButton(
                onClick = { viewModel.toggleFlash() },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    when (flashMode) {
                        FlashMode.OFF -> Icons.Default.FlashOff
                        FlashMode.ON -> Icons.Default.FlashOn
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                    },
                    contentDescription = "Flash",
                    tint = Color.White
                )
            }
        }
        
        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Speed control
            SpeedControlPanel(
                selectedSpeed = viewModel.selectedSpeed.collectAsStateWithLifecycle().value,
                onSpeedSelected = { speed -> viewModel.setRecordingSpeed(speed) }
            )
            
            // Main controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery button
                IconButton(
                    onClick = { /* Open gallery */ },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(56.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Record button
                RecordButton(
                    isRecording = isRecording,
                    onClick = {
                        if (isRecording) {
                            viewModel.stopRecording { projectId ->
                                onNavigateToEditor(projectId)
                            }
                        } else {
                            viewModel.startRecording()
                        }
                    }
                )
                
                // Camera flip button
                IconButton(
                    onClick = { viewModel.flipCamera() },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Cameraswitch,
                        contentDescription = "Flip Camera",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        // Side tools
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SideToolButton(
                icon = Icons.Default.Timer,
                label = "Timer",
                onClick = { /* Timer functionality */ }
            )
            SideToolButton(
                icon = Icons.Default.FilterList,
                label = "Filters",
                onClick = { /* Filters */ }
            )
            SideToolButton(
                icon = Icons.Default.Beauty,
                label = "Beauty",
                onClick = { /* Beauty mode */ }
            )
            SideToolButton(
                icon = Icons.Default.GridOn,
                label = "Grid",
                onClick = { viewModel.toggleGrid() }
            )
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onCameraReady: () -> Unit
) {
    // This would integrate with CameraX
    // For now, showing a placeholder
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Camera Preview",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun RecordButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color.White.copy(alpha = 0.3f),
                    CircleShape
                )
        )
        
        // Inner button
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (isRecording) Color.Red else Color.White,
                    if (isRecording) RoundedCornerShape(12.dp) else CircleShape
                )
        ) {
            if (!isRecording) {
                Icon(
                    Icons.Default.FiberManualRecord,
                    contentDescription = "Record",
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun SpeedControlPanel(
    selectedSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val speeds = listOf(0.3f, 0.5f, 1f, 2f, 3f)
        speeds.forEach { speed ->
            val isSelected = selectedSpeed == speed
            val label = when (speed) {
                0.3f -> "0.3x"
                0.5f -> "0.5x"
                1f -> "1x"
                2f -> "2x"
                3f -> "3x"
                else -> "${speed}x"
            }
            
            FilterChip(
                selected = isSelected,
                onClick = { onSpeedSelected(speed) },
                label = {
                    Text(
                        label,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    selectedContainerColor = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}

@Composable
fun SideToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .size(48.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

enum class FlashMode {
    OFF, ON, AUTO
}

fun formatRecordingTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}