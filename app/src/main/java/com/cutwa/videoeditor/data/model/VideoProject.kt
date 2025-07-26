package com.cutwa.videoeditor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "video_projects")
@TypeConverters(Converters::class)
@Serializable
data class VideoProject(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdAt: Long,
    val lastModified: Long,
    val thumbnailPath: String?,
    val duration: Long,
    val aspectRatio: AspectRatio,
    val resolution: Resolution,
    val frameRate: Int,
    val tracks: List<Track> = emptyList()
)

@Serializable
data class Track(
    val id: String,
    val type: TrackType,
    val name: String,
    val isVisible: Boolean = true,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val clips: List<TimelineClip> = emptyList()
)

@Serializable
sealed class TimelineClip {
    abstract val id: String
    abstract val startTime: Long
    abstract val endTime: Long
    abstract val trackId: String
    
    @Serializable
    data class VideoClip(
        override val id: String,
        override val startTime: Long,
        override val endTime: Long,
        override val trackId: String,
        val filePath: String,
        val trimStart: Long = 0,
        val trimEnd: Long = 0,
        val speed: Float = 1.0f,
        val effects: List<Effect> = emptyList(),
        val filters: List<Filter> = emptyList(),
        val keyframes: List<Keyframe> = emptyList()
    ) : TimelineClip()
    
    @Serializable
    data class AudioClip(
        override val id: String,
        override val startTime: Long,
        override val endTime: Long,
        override val trackId: String,
        val filePath: String,
        val volume: Float = 1.0f,
        val fadeIn: Long = 0,
        val fadeOut: Long = 0,
        val effects: List<AudioEffect> = emptyList()
    ) : TimelineClip()
    
    @Serializable
    data class TextClip(
        override val id: String,
        override val startTime: Long,
        override val endTime: Long,
        override val trackId: String,
        val text: String,
        val fontFamily: String,
        val fontSize: Float,
        val color: String,
        val position: Position,
        val animation: TextAnimation? = null,
        val keyframes: List<Keyframe> = emptyList()
    ) : TimelineClip()
    
    @Serializable
    data class ImageClip(
        override val id: String,
        override val startTime: Long,
        override val endTime: Long,
        override val trackId: String,
        val filePath: String,
        val position: Position,
        val scale: Float = 1.0f,
        val rotation: Float = 0.0f,
        val keyframes: List<Keyframe> = emptyList()
    ) : TimelineClip()
}

@Serializable
data class Effect(
    val id: String,
    val type: EffectType,
    val intensity: Float = 1.0f,
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
data class Filter(
    val id: String,
    val type: FilterType,
    val intensity: Float = 1.0f,
    val parameters: Map<String, Float> = emptyMap()
)

@Serializable
data class AudioEffect(
    val id: String,
    val type: AudioEffectType,
    val intensity: Float = 1.0f,
    val parameters: Map<String, Float> = emptyMap()
)

@Serializable
data class Keyframe(
    val id: String,
    val time: Long,
    val property: String,
    val value: Float,
    val interpolationType: InterpolationType = InterpolationType.LINEAR
)

@Serializable
data class Position(
    val x: Float,
    val y: Float
)

@Serializable
data class Resolution(
    val width: Int,
    val height: Int
)

@Serializable
enum class AspectRatio(val ratio: String, val width: Int, val height: Int) {
    RATIO_16_9("16:9", 16, 9),
    RATIO_9_16("9:16", 9, 16),
    RATIO_4_3("4:3", 4, 3),
    RATIO_1_1("1:1", 1, 1),
    RATIO_21_9("21:9", 21, 9)
}

@Serializable
enum class TrackType {
    VIDEO,
    AUDIO,
    TEXT,
    OVERLAY
}

@Serializable
enum class EffectType {
    BLUR,
    SHARPEN,
    GLITCH,
    VINTAGE,
    NEON,
    RAINBOW,
    PARTICLE,
    TRANSITION_FADE,
    TRANSITION_SLIDE,
    TRANSITION_ZOOM,
    TRANSITION_DISSOLVE
}

@Serializable
enum class FilterType {
    BRIGHTNESS,
    CONTRAST,
    SATURATION,
    HUE,
    EXPOSURE,
    SHADOWS,
    HIGHLIGHTS,
    TEMPERATURE,
    TINT,
    VIGNETTE,
    LUT
}

@Serializable
enum class AudioEffectType {
    REVERB,
    ECHO,
    BASS_BOOST,
    TREBLE_BOOST,
    NOISE_REDUCTION,
    PITCH_SHIFT,
    TIME_STRETCH
}

@Serializable
enum class TextAnimation {
    FADE_IN,
    SLIDE_IN_LEFT,
    SLIDE_IN_RIGHT,
    SLIDE_IN_UP,
    SLIDE_IN_DOWN,
    ZOOM_IN,
    TYPEWRITER,
    BOUNCE
}

@Serializable
enum class InterpolationType {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT,
    BEZIER
}

class Converters {
    @TypeConverter
    fun fromTracks(tracks: List<Track>): String {
        return Json.encodeToString(tracks)
    }
    
    @TypeConverter
    fun toTracks(tracksString: String): List<Track> {
        return Json.decodeFromString(tracksString)
    }
    
    @TypeConverter
    fun fromAspectRatio(aspectRatio: AspectRatio): String {
        return aspectRatio.name
    }
    
    @TypeConverter
    fun toAspectRatio(aspectRatioString: String): AspectRatio {
        return AspectRatio.valueOf(aspectRatioString)
    }
    
    @TypeConverter
    fun fromResolution(resolution: Resolution): String {
        return Json.encodeToString(resolution)
    }
    
    @TypeConverter
    fun toResolution(resolutionString: String): Resolution {
        return Json.decodeFromString(resolutionString)
    }
}