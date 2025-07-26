package com.cutwa.videoeditor.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import androidx.annotation.WorkerThread
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class VideoInfo(
    val duration: Long,
    val width: Int,
    val height: Int,
    val frameRate: Int,
    val bitrate: Long
)

@Singleton
class VideoUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val projectsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "Cutwa")

    init {
        if (!projectsDir.exists()) {
            projectsDir.mkdirs()
        }
    }

    @WorkerThread
    suspend fun getVideoInfo(videoUri: Uri): VideoInfo = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull()?.toInt() ?: 30
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
            
            VideoInfo(duration, width, height, frameRate, bitrate)
        } finally {
            retriever.release()
        }
    }

    @WorkerThread
    suspend fun generateThumbnail(videoUri: Uri, projectId: String): String = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            
            if (bitmap != null) {
                val thumbnailDir = File(projectsDir, projectId)
                if (!thumbnailDir.exists()) {
                    thumbnailDir.mkdirs()
                }
                
                val thumbnailFile = File(thumbnailDir, "thumbnail.jpg")
                val outputStream = FileOutputStream(thumbnailFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.close()
                bitmap.recycle()
                
                thumbnailFile.absolutePath
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            retriever.release()
        }
    }

    @WorkerThread
    suspend fun extractFrameAt(videoUri: Uri, timeUs: Long): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    @WorkerThread
    suspend fun trimVideo(
        inputPath: String,
        outputPath: String,
        startTimeMs: Long,
        endTimeMs: Long,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val startTimeSeconds = startTimeMs / 1000.0
        val duration = (endTimeMs - startTimeMs) / 1000.0
        
        val command = "-i \"$inputPath\" -ss $startTimeSeconds -t $duration -c copy \"$outputPath\""
        
        var success = false
        val session: FFmpegSession = FFmpegKit.execute(command)
        
        if (ReturnCode.isSuccess(session.returnCode)) {
            success = true
        }
        
        success
    }

    @WorkerThread
    suspend fun mergeVideos(
        inputPaths: List<String>,
        outputPath: String,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val inputsCommand = inputPaths.joinToString(" ") { "-i \"$it\"" }
        val filterComplex = buildString {
            append("-filter_complex \"")
            for (i in inputPaths.indices) {
                append("[$i:v][$i:a]")
            }
            append("concat=n=${inputPaths.size}:v=1:a=1[outv][outa]\"")
        }
        
        val command = "$inputsCommand $filterComplex -map \"[outv]\" -map \"[outa]\" \"$outputPath\""
        
        var success = false
        val session: FFmpegSession = FFmpegKit.execute(command)
        
        if (ReturnCode.isSuccess(session.returnCode)) {
            success = true
        }
        
        success
    }

    @WorkerThread
    suspend fun applyFilter(
        inputPath: String,
        outputPath: String,
        filterName: String,
        parameters: Map<String, Any>,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val filterString = buildFilterString(filterName, parameters)
        val command = "-i \"$inputPath\" -vf \"$filterString\" \"$outputPath\""
        
        var success = false
        val session: FFmpegSession = FFmpegKit.execute(command)
        
        if (ReturnCode.isSuccess(session.returnCode)) {
            success = true
        }
        
        success
    }

    @WorkerThread
    suspend fun changeSpeed(
        inputPath: String,
        outputPath: String,
        speed: Float,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val videoSpeed = speed
        val audioSpeed = speed
        
        val command = "-i \"$inputPath\" -filter_complex \"[0:v]setpts=PTS/${videoSpeed}[v];[0:a]atempo=${audioSpeed}[a]\" -map \"[v]\" -map \"[a]\" \"$outputPath\""
        
        var success = false
        val session: FFmpegSession = FFmpegKit.execute(command)
        
        if (ReturnCode.isSuccess(session.returnCode)) {
            success = true
        }
        
        success
    }

    @WorkerThread
    suspend fun addTransition(
        input1Path: String,
        input2Path: String,
        outputPath: String,
        transitionType: String,
        duration: Float,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val transitionFilter = when (transitionType) {
            "fade" -> "fade"
            "dissolve" -> "xfade=transition=fade:duration=$duration"
            "wipeleft" -> "xfade=transition=wipeleft:duration=$duration"
            "slideright" -> "xfade=transition=slideright:duration=$duration"
            else -> "xfade=transition=fade:duration=$duration"
        }
        
        val command = "-i \"$input1Path\" -i \"$input2Path\" -filter_complex \"[$transitionFilter]\" \"$outputPath\""
        
        var success = false
        val session: FFmpegSession = FFmpegKit.execute(command)
        
        if (ReturnCode.isSuccess(session.returnCode)) {
            success = true
        }
        
        success
    }

    @WorkerThread
    suspend fun exportVideo(
        inputPath: String,
        outputPath: String,
        resolution: String = "1920x1080",
        bitrate: String = "5000k",
        frameRate: Int = 30,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val command = "-i \"$inputPath\" -vf scale=$resolution -b:v $bitrate -r $frameRate \"$outputPath\""
        
        var success = false
        val session: FFmpegSession = FFmpegKit.execute(command)
        
        if (ReturnCode.isSuccess(session.returnCode)) {
            success = true
        }
        
        success
    }

    fun getProjectDir(projectId: String): File {
        return File(projectsDir, projectId)
    }

    fun deleteProjectFiles(projectId: String) {
        val projectDir = File(projectsDir, projectId)
        if (projectDir.exists()) {
            projectDir.deleteRecursively()
        }
    }

    private fun buildFilterString(filterName: String, parameters: Map<String, Any>): String {
        return when (filterName) {
            "brightness" -> "eq=brightness=${parameters["value"] ?: 0.0}"
            "contrast" -> "eq=contrast=${parameters["value"] ?: 1.0}"
            "saturation" -> "eq=saturation=${parameters["value"] ?: 1.0}"
            "hue" -> "eq=hue=${parameters["value"] ?: 0.0}"
            "blur" -> "gblur=sigma=${parameters["sigma"] ?: 1.0}"
            "sharpen" -> "unsharp=5:5:${parameters["amount"] ?: 1.0}:5:5:0.0"
            "vignette" -> "vignette=PI/4"
            "sepia" -> "colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131"
            else -> filterName
        }
    }
}