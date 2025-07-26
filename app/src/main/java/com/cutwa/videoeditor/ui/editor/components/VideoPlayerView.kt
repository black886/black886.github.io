package com.cutwa.videoeditor.ui.editor.components

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.cutwa.videoeditor.data.model.VideoProject
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView

@Composable
fun VideoPlayerView(
    project: VideoProject,
    isPlaying: Boolean,
    currentPosition: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    
    // Set up video sources from project tracks
    LaunchedEffect(project) {
        val videoTracks = project.tracks.filter { it.type == com.cutwa.videoeditor.data.model.TrackType.VIDEO }
        if (videoTracks.isNotEmpty()) {
            val firstVideoItem = videoTracks.first().items.firstOrNull()
            if (firstVideoItem != null) {
                val mediaItem = MediaItem.fromUri(Uri.parse(firstVideoItem.sourceUri))
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            }
        }
    }
    
    // Control playback state
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }
    
    // Seek to position
    LaunchedEffect(currentPosition) {
        if (exoPlayer.currentPosition != currentPosition) {
            exoPlayer.seekTo(currentPosition)
        }
    }
    
    // Clean up
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    AndroidView(
        factory = { context ->
            StyledPlayerView(context).apply {
                player = exoPlayer
                useController = false // We have our own controls
                setShowBuffering(StyledPlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}