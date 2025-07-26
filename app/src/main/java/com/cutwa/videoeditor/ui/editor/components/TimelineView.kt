package com.cutwa.videoeditor.ui.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cutwa.videoeditor.data.model.*

@Composable
fun TimelineView(
    project: VideoProject?,
    currentPosition: Long,
    onItemSelected: (TrackItem) -> Unit,
    onItemMoved: (TrackItem, Long) -> Unit,
    onItemTrimmed: (TrackItem, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val timelineScale = remember { mutableStateOf(1f) } // pixels per millisecond
    val scrollPosition = remember { mutableStateOf(0f) }
    
    if (project == null) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("No project loaded", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        // Timeline ruler
        TimelineRuler(
            duration = project.duration,
            currentPosition = currentPosition,
            scale = timelineScale.value,
            scrollPosition = scrollPosition.value,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        )

        // Tracks
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(project.tracks) { track ->
                TrackRow(
                    track = track,
                    projectDuration = project.duration,
                    currentPosition = currentPosition,
                    scale = timelineScale.value,
                    scrollPosition = scrollPosition.value,
                    onItemSelected = onItemSelected,
                    onItemMoved = onItemMoved,
                    onItemTrimmed = onItemTrimmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }
    }
}

@Composable
fun TimelineRuler(
    duration: Long,
    currentPosition: Long,
    scale: Float,
    scrollPosition: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Draw background
        drawRect(
            color = Color.Gray.copy(alpha = 0.1f),
            size = size
        )
        
        // Draw time markers
        val stepMs = 5000L // 5 second intervals
        val stepPx = stepMs * scale
        
        var timeMs = 0L
        while (timeMs <= duration) {
            val x = timeMs * scale - scrollPosition
            if (x >= 0 && x <= width) {
                // Draw tick mark
                drawLine(
                    color = Color.Gray,
                    start = Offset(x, height * 0.7f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw time label (every 10 seconds)
                if (timeMs % 10000 == 0L) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(x, height * 0.3f),
                        end = Offset(x, height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            timeMs += stepMs
        }
        
        // Draw playhead
        val playheadX = currentPosition * scale - scrollPosition
        if (playheadX >= 0 && playheadX <= width) {
            drawLine(
                color = Color.Red,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
fun TrackRow(
    track: Track,
    projectDuration: Long,
    currentPosition: Long,
    scale: Float,
    scrollPosition: Float,
    onItemSelected: (TrackItem) -> Unit,
    onItemMoved: (TrackItem, Long) -> Unit,
    onItemTrimmed: (TrackItem, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Track background
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = when (track.type) {
                    TrackType.VIDEO -> Color(0xFF3B82F6).copy(alpha = 0.1f)
                    TrackType.AUDIO -> Color(0xFF10B981).copy(alpha = 0.1f)
                    TrackType.TEXT -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                    TrackType.OVERLAY -> Color(0xFF8B5CF6).copy(alpha = 0.1f)
                    TrackType.STICKER -> Color(0xFFEF4444).copy(alpha = 0.1f)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track type icon
                Icon(
                    imageVector = when (track.type) {
                        TrackType.VIDEO -> Icons.Default.Videocam
                        TrackType.AUDIO -> Icons.Default.MusicNote
                        TrackType.TEXT -> Icons.Default.TextFields
                        TrackType.OVERLAY -> Icons.Default.Layers
                        TrackType.STICKER -> Icons.Default.EmojiEmotions
                    },
                    contentDescription = track.type.name,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Track items
                Box(modifier = Modifier.weight(1f)) {
                    track.items.forEach { item ->
                        TrackItemView(
                            item = item,
                            trackType = track.type,
                            scale = scale,
                            scrollPosition = scrollPosition,
                            onItemSelected = { onItemSelected(item) },
                            onItemMoved = { newPosition -> onItemMoved(item, newPosition) },
                            onItemTrimmed = { trimStart, trimEnd -> onItemTrimmed(item, trimStart, trimEnd) }
                        )
                    }
                }
                
                // Track controls
                Row {
                    IconButton(
                        onClick = { /* Toggle visibility */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (track.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    if (track.type == TrackType.AUDIO || track.type == TrackType.VIDEO) {
                        IconButton(
                            onClick = { /* Toggle mute */ },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                if (track.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Toggle mute",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackItemView(
    item: TrackItem,
    trackType: TrackType,
    scale: Float,
    scrollPosition: Float,
    onItemSelected: () -> Unit,
    onItemMoved: (Long) -> Unit,
    onItemTrimmed: (Long, Long) -> Unit
) {
    val density = LocalDensity.current
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    
    val itemStartX = item.startTime * scale - scrollPosition
    val itemWidth = item.duration * scale
    
    if (itemStartX + itemWidth > 0 && itemStartX < 1000) { // Only render visible items
        Card(
            modifier = Modifier
                .offset(x = with(density) { (itemStartX + dragOffset).toDp() })
                .width(with(density) { itemWidth.toDp() })
                .fillMaxHeight()
                .pointerInput(item.id) {
                    detectDragGestures(
                        onDragStart = { 
                            isDragging = true
                            onItemSelected()
                        },
                        onDragEnd = { 
                            isDragging = false
                            if (dragOffset != 0f) {
                                val newStartTime = item.startTime + (dragOffset / scale).toLong()
                                onItemMoved(newStartTime.coerceAtLeast(0))
                                dragOffset = 0f
                            }
                        },
                        onDrag = { delta ->
                            dragOffset += delta.x
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = when (trackType) {
                    TrackType.VIDEO -> Color(0xFF3B82F6)
                    TrackType.AUDIO -> Color(0xFF10B981)
                    TrackType.TEXT -> Color(0xFFF59E0B)
                    TrackType.OVERLAY -> Color(0xFF8B5CF6)
                    TrackType.STICKER -> Color(0xFFEF4444)
                }.copy(alpha = if (isDragging) 0.7f else 1f)
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (item.type) {
                    TrackItemType.VIDEO_CLIP -> {
                        // Video thumbnail or waveform
                        AsyncImage(
                            model = item.sourceUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    TrackItemType.AUDIO_CLIP -> {
                        // Audio waveform visualization
                        AudioWaveform(
                            modifier = Modifier.fillMaxSize(),
                            audioUri = item.sourceUri
                        )
                    }
                    TrackItemType.TEXT -> {
                        // Text preview
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.sourceUri.take(10) + if (item.sourceUri.length > 10) "..." else "",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    TrackItemType.IMAGE -> {
                        AsyncImage(
                            model = item.sourceUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    TrackItemType.STICKER -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEmotions,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    TrackItemType.EFFECT -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Trim handles
                if (isDragging) {
                    // Left trim handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(8.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.8f))
                    )
                    
                    // Right trim handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(8.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}

@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    audioUri: String
) {
    // Simplified waveform visualization
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        // Draw simplified waveform
        for (i in 0 until width.toInt() step 4) {
            val amplitude = (Math.sin(i * 0.1) * centerY * 0.8).toFloat()
            drawLine(
                color = Color.White,
                start = Offset(i.toFloat(), centerY - amplitude),
                end = Offset(i.toFloat(), centerY + amplitude),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}