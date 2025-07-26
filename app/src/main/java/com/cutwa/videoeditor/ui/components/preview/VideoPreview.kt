package com.cutwa.videoeditor.ui.components.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cutwa.videoeditor.data.model.VideoProject
import com.cutwa.videoeditor.ui.theme.*

@Composable
fun VideoPreview(
    modifier: Modifier = Modifier,
    project: VideoProject,
    currentTime: Long,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Box(
        modifier = modifier
            .background(Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Video preview area (placeholder for actual video rendering)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkGray),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for video content
            Text(
                text = "Video Preview\n${project.resolution.width}x${project.resolution.height}",
                color = White.copy(alpha = 0.7f)
            )
        }
        
        // Play/Pause overlay button
        FloatingActionButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            containerColor = AccentBlue.copy(alpha = 0.8f),
            contentColor = White
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}