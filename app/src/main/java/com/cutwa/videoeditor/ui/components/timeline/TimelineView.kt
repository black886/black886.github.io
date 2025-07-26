package com.cutwa.videoeditor.ui.components.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cutwa.videoeditor.data.model.*
import com.cutwa.videoeditor.ui.theme.*

@Composable
fun TimelineView(
    modifier: Modifier = Modifier,
    project: VideoProject,
    currentTime: Long,
    selectedClip: TimelineClip?,
    onSeek: (Long) -> Unit,
    onClipSelected: (TimelineClip) -> Unit,
    onClipMoved: (String, Long) -> Unit,
    onClipTrimmed: (String, Long, Long) -> Unit,
    onClipSplit: (String, Long) -> Unit,
    onAddClip: (String) -> Unit
) {
    Column(
        modifier = modifier.background(TimelineBackground)
    ) {
        // Timeline header with time ruler
        TimelineRuler(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            duration = project.duration,
            currentTime = currentTime,
            onSeek = onSeek
        )
        
        // Timeline tracks
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(project.tracks) { track ->
                TimelineTrack(
                    track = track,
                    duration = project.duration,
                    currentTime = currentTime,
                    selectedClip = selectedClip,
                    onClipSelected = onClipSelected,
                    onClipMoved = onClipMoved,
                    onClipTrimmed = onClipTrimmed,
                    onAddClip = { onAddClip(track.id) }
                )
            }
        }
    }
}

@Composable
private fun TimelineRuler(
    modifier: Modifier = Modifier,
    duration: Long,
    currentTime: Long,
    onSeek: (Long) -> Unit
) {
    Canvas(
        modifier = modifier.background(TimelineBackground)
    ) {
        drawTimelineRuler(duration, currentTime, onSeek)
    }
}

@Composable
private fun TimelineTrack(
    track: Track,
    duration: Long,
    currentTime: Long,
    selectedClip: TimelineClip?,
    onClipSelected: (TimelineClip) -> Unit,
    onClipMoved: (String, Long) -> Unit,
    onClipTrimmed: (String, Long, Long) -> Unit,
    onAddClip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(TimelineTrack),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track header
        Surface(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight(),
            color = ToolBackground
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = track.name,
                    color = White,
                    fontSize = 12.sp
                )
                
                IconButton(
                    onClick = onAddClip,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add clip",
                        tint = White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // Track content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(TimelineTrack)
        ) {
            // Draw clips
            track.clips.forEach { clip ->
                TimelineClip(
                    clip = clip,
                    duration = duration,
                    isSelected = selectedClip?.id == clip.id,
                    onClipSelected = { onClipSelected(clip) },
                    onClipMoved = onClipMoved,
                    onClipTrimmed = onClipTrimmed
                )
            }
            
            // Draw playhead
            val playheadPosition = (currentTime.toFloat() / duration.toFloat())
            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = playheadPosition * size.width
                drawLine(
                    color = TimelinePlayhead,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun TimelineClip(
    clip: TimelineClip,
    duration: Long,
    isSelected: Boolean,
    onClipSelected: (TimelineClip) -> Unit,
    onClipMoved: (String, Long) -> Unit,
    onClipTrimmed: (String, Long, Long) -> Unit
) {
    val startPercent = clip.startTime.toFloat() / duration.toFloat()
    val endPercent = clip.endTime.toFloat() / duration.toFloat()
    val clipWidth = endPercent - startPercent
    
    val clipColor = when (clip) {
        is TimelineClip.VideoClip -> AccentBlue
        is TimelineClip.AudioClip -> AccentGreen
        is TimelineClip.TextClip -> AccentPurple
        is TimelineClip.ImageClip -> AccentPink
    }
    
    BoxWithConstraints {
        val clipWidthPx = maxWidth * clipWidth
        val clipStartPx = maxWidth * startPercent
        
        Surface(
            modifier = Modifier
                .offset(x = clipStartPx)
                .width(clipWidthPx)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
            color = if (isSelected) clipColor.copy(alpha = 0.8f) else clipColor.copy(alpha = 0.6f),
            onClick = { onClipSelected(clip) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = when (clip) {
                        is TimelineClip.VideoClip -> "Video"
                        is TimelineClip.AudioClip -> "Audio"
                        is TimelineClip.TextClip -> clip.text
                        is TimelineClip.ImageClip -> "Image"
                    },
                    color = White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun DrawScope.drawTimelineRuler(
    duration: Long,
    currentTime: Long,
    onSeek: (Long) -> Unit
) {
    val tickColor = White.copy(alpha = 0.5f)
    val textColor = White
    
    // Draw background
    drawRect(
        color = TimelineBackground,
        size = size
    )
    
    // Draw time ticks
    val tickCount = 10
    val tickInterval = duration / tickCount
    
    for (i in 0..tickCount) {
        val time = i * tickInterval
        val x = (time.toFloat() / duration.toFloat()) * size.width
        
        drawLine(
            color = tickColor,
            start = Offset(x, size.height * 0.7f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // Draw playhead
    val playheadX = (currentTime.toFloat() / duration.toFloat()) * size.width
    drawLine(
        color = TimelinePlayhead,
        start = Offset(playheadX, 0f),
        end = Offset(playheadX, size.height),
        strokeWidth = 3.dp.toPx()
    )
}