package com.cutwa.videoeditor.engine

import android.content.Context
import android.net.Uri
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.cutwa.videoeditor.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoProcessor @Inject constructor(
    private val context: Context
) {
    
    data class ProcessingProgress(
        val percentage: Float,
        val currentOperation: String,
        val estimatedTimeRemaining: Long
    )
    
    suspend fun processVideo(
        project: VideoProject,
        outputPath: String,
        onProgress: (ProcessingProgress) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Initialize FFmpeg configuration
            Config.enableLogCallback { log ->
                // Parse progress from FFmpeg logs
                parseProgress(log.text)?.let { progress ->
                    onProgress(progress)
                }
            }
            
            val tempDir = File(context.cacheDir, "video_processing")
            tempDir.mkdirs()
            
            // Process each track and merge
            val processedTracks = mutableListOf<String>()
            
            project.tracks.forEach { track ->
                when (track.type) {
                    TrackType.VIDEO -> {
                        val videoTrackPath = processVideoTrack(track, tempDir)
                        processedTracks.add(videoTrackPath)
                    }
                    TrackType.AUDIO -> {
                        val audioTrackPath = processAudioTrack(track, tempDir)
                        processedTracks.add(audioTrackPath)
                    }
                    TrackType.TEXT -> {
                        // Text overlays will be processed during video track processing
                    }
                    TrackType.OVERLAY -> {
                        // Overlays will be processed during video track processing
                    }
                }
            }
            
            // Merge all tracks
            val finalVideoPath = mergeAllTracks(processedTracks, project, outputPath)
            
            // Clean up temporary files
            tempDir.deleteRecursively()
            
            Result.success(finalVideoPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun processVideoTrack(track: Track, tempDir: File): String {
        val outputFile = File(tempDir, "video_track_${track.id}.mp4")
        val videoClips = track.clips.filterIsInstance<TimelineClip.VideoClip>()
        
        val filterComplex = buildVideoFilterComplex(videoClips, track)
        
        val command = buildString {
            // Input files
            videoClips.forEach { clip ->
                append("-i \"${clip.filePath}\" ")
            }
            
            // Filter complex
            append("-filter_complex \"$filterComplex\" ")
            
            // Output settings
            append("-c:v libx264 -preset medium -crf 23 ")
            append("-c:a aac -b:a 128k ")
            append("\"${outputFile.absolutePath}\"")
        }
        
        val result = FFmpeg.execute(command)
        if (result != Config.RETURN_CODE_SUCCESS) {
            throw RuntimeException("Video processing failed with code: $result")
        }
        
        return outputFile.absolutePath
    }
    
    private fun processAudioTrack(track: Track, tempDir: File): String {
        val outputFile = File(tempDir, "audio_track_${track.id}.wav")
        val audioClips = track.clips.filterIsInstance<TimelineClip.AudioClip>()
        
        val filterComplex = buildAudioFilterComplex(audioClips, track)
        
        val command = buildString {
            audioClips.forEach { clip ->
                append("-i \"${clip.filePath}\" ")
            }
            
            append("-filter_complex \"$filterComplex\" ")
            append("-c:a pcm_s16le ")
            append("\"${outputFile.absolutePath}\"")
        }
        
        val result = FFmpeg.execute(command)
        if (result != Config.RETURN_CODE_SUCCESS) {
            throw RuntimeException("Audio processing failed with code: $result")
        }
        
        return outputFile.absolutePath
    }
    
    private fun buildVideoFilterComplex(clips: List<TimelineClip.VideoClip>, track: Track): String {
        val filters = mutableListOf<String>()
        
        clips.forEachIndexed { index, clip ->
            val inputLabel = "[$index:v]"
            val outputLabel = "[v$index]"
            
            val clipFilters = mutableListOf<String>()
            
            // Apply speed changes
            if (clip.speed != 1.0f) {
                clipFilters.add("setpts=${1.0f / clip.speed}*PTS")
            }
            
            // Apply filters
            clip.filters.forEach { filter ->
                when (filter.type) {
                    FilterType.BRIGHTNESS -> clipFilters.add("eq=brightness=${filter.intensity}")
                    FilterType.CONTRAST -> clipFilters.add("eq=contrast=${filter.intensity}")
                    FilterType.SATURATION -> clipFilters.add("eq=saturation=${filter.intensity}")
                    FilterType.HUE -> clipFilters.add("hue=h=${filter.intensity}")
                    FilterType.BLUR -> clipFilters.add("boxblur=${filter.intensity}")
                    else -> {} // Add more filters as needed
                }
            }
            
            // Apply effects
            clip.effects.forEach { effect ->
                when (effect.type) {
                    EffectType.VINTAGE -> clipFilters.add("curves=vintage")
                    EffectType.NEON -> clipFilters.add("colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131")
                    else -> {} // Add more effects as needed
                }
            }
            
            if (clipFilters.isNotEmpty()) {
                filters.add("$inputLabel${clipFilters.joinToString(",")}$outputLabel")
            }
        }
        
        return filters.joinToString(";")
    }
    
    private fun buildAudioFilterComplex(clips: List<TimelineClip.AudioClip>, track: Track): String {
        val filters = mutableListOf<String>()
        
        clips.forEachIndexed { index, clip ->
            val inputLabel = "[$index:a]"
            val outputLabel = "[a$index]"
            
            val clipFilters = mutableListOf<String>()
            
            // Apply volume
            if (clip.volume != 1.0f) {
                clipFilters.add("volume=${clip.volume}")
            }
            
            // Apply fade in/out
            if (clip.fadeIn > 0) {
                clipFilters.add("afade=t=in:ss=0:d=${clip.fadeIn / 1000.0}")
            }
            if (clip.fadeOut > 0) {
                clipFilters.add("afade=t=out:st=${(clip.endTime - clip.startTime - clip.fadeOut) / 1000.0}:d=${clip.fadeOut / 1000.0}")
            }
            
            // Apply effects
            clip.effects.forEach { effect ->
                when (effect.type) {
                    AudioEffectType.REVERB -> clipFilters.add("aecho=0.8:0.88:60:0.4")
                    AudioEffectType.BASS_BOOST -> clipFilters.add("bass=g=10")
                    AudioEffectType.TREBLE_BOOST -> clipFilters.add("treble=g=5")
                    else -> {} // Add more audio effects
                }
            }
            
            if (clipFilters.isNotEmpty()) {
                filters.add("$inputLabel${clipFilters.joinToString(",")}$outputLabel")
            }
        }
        
        return filters.joinToString(";")
    }
    
    private fun mergeAllTracks(
        trackPaths: List<String>,
        project: VideoProject,
        outputPath: String
    ): String {
        val command = buildString {
            trackPaths.forEach { path ->
                append("-i \"$path\" ")
            }
            
            // Set output resolution and frame rate
            append("-s ${project.resolution.width}x${project.resolution.height} ")
            append("-r ${project.frameRate} ")
            
            // Video codec settings
            append("-c:v libx264 -preset medium -crf 18 ")
            append("-c:a aac -b:a 192k ")
            
            // Output format
            append("-f mp4 ")
            append("\"$outputPath\"")
        }
        
        val result = FFmpeg.execute(command)
        if (result != Config.RETURN_CODE_SUCCESS) {
            throw RuntimeException("Final merge failed with code: $result")
        }
        
        return outputPath
    }
    
    private fun parseProgress(logText: String): ProcessingProgress? {
        // Parse FFmpeg log to extract progress information
        val timeRegex = Regex("time=([0-9:.]+)")
        val timeMatch = timeRegex.find(logText)
        
        return timeMatch?.let { match ->
            val timeString = match.groupValues[1]
            val timeParts = timeString.split(":")
            val seconds = timeParts[0].toFloat() * 3600 + 
                         timeParts[1].toFloat() * 60 + 
                         timeParts[2].toFloat()
            
            ProcessingProgress(
                percentage = (seconds / 100) * 100, // Simplified calculation
                currentOperation = "Processing video...",
                estimatedTimeRemaining = 0
            )
        }
    }
    
    suspend fun extractThumbnail(videoPath: String, timePosition: Long): String? = withContext(Dispatchers.IO) {
        try {
            val thumbnailFile = File(context.cacheDir, "thumbnail_${System.currentTimeMillis()}.jpg")
            
            val command = "-i \"$videoPath\" -ss ${timePosition / 1000.0} -vframes 1 \"${thumbnailFile.absolutePath}\""
            
            val result = FFmpeg.execute(command)
            if (result == Config.RETURN_CODE_SUCCESS) {
                thumbnailFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getVideoInfo(videoPath: String): VideoInfo? = withContext(Dispatchers.IO) {
        try {
            // Use FFprobe to get video information
            val command = "-v quiet -print_format json -show_format -show_streams \"$videoPath\""
            
            // This would require parsing FFprobe output
            // For now, return a basic VideoInfo
            VideoInfo(
                duration = 0,
                width = 1920,
                height = 1080,
                frameRate = 30.0,
                bitrate = 0
            )
        } catch (e: Exception) {
            null
        }
    }
    
    data class VideoInfo(
        val duration: Long,
        val width: Int,
        val height: Int,
        val frameRate: Double,
        val bitrate: Long
    )
}