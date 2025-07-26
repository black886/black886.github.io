package com.cutwa.videoeditor.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize

@Entity(tableName = "video_projects")
@TypeConverters(Converters::class)
@Parcelize
data class VideoProject(
    @PrimaryKey
    val id: String,
    val name: String,
    val thumbnailPath: String,
    val duration: Long, // in milliseconds
    val aspectRatio: AspectRatio,
    val resolution: Resolution,
    val frameRate: Int,
    val tracks: List<Track>,
    val createdAt: Long,
    val updatedAt: Long,
    val exportPath: String? = null
) : Parcelable

@Parcelize
data class Track(
    val id: String,
    val type: TrackType,
    val items: List<TrackItem>,
    val isVisible: Boolean = true,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f
) : Parcelable

@Parcelize
data class TrackItem(
    val id: String,
    val type: TrackItemType,
    val startTime: Long, // in milliseconds
    val duration: Long, // in milliseconds
    val sourceUri: String,
    val trimStart: Long = 0L,
    val trimEnd: Long = 0L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val filters: List<Filter> = emptyList(),
    val effects: List<Effect> = emptyList(),
    val transitions: List<Transition> = emptyList(),
    val keyframes: List<Keyframe> = emptyList(),
    val transform: Transform = Transform()
) : Parcelable

@Parcelize
data class Transform(
    val x: Float = 0f,
    val y: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f
) : Parcelable

@Parcelize
data class Filter(
    val id: String,
    val type: FilterType,
    val intensity: Float = 1.0f,
    val parameters: Map<String, Any> = emptyMap()
) : Parcelable

@Parcelize
data class Effect(
    val id: String,
    val type: EffectType,
    val startTime: Long,
    val duration: Long,
    val parameters: Map<String, Any> = emptyMap()
) : Parcelable

@Parcelize
data class Transition(
    val id: String,
    val type: TransitionType,
    val duration: Long,
    val parameters: Map<String, Any> = emptyMap()
) : Parcelable

@Parcelize
data class Keyframe(
    val id: String,
    val property: String,
    val time: Long,
    val value: Any,
    val interpolation: InterpolationType = InterpolationType.LINEAR
) : Parcelable

@Parcelize
data class AspectRatio(
    val width: Int,
    val height: Int
) : Parcelable {
    val ratio: Float get() = width.toFloat() / height.toFloat()
}

@Parcelize
data class Resolution(
    val width: Int,
    val height: Int
) : Parcelable

enum class TrackType {
    VIDEO, AUDIO, TEXT, OVERLAY, STICKER
}

enum class TrackItemType {
    VIDEO_CLIP, AUDIO_CLIP, TEXT, IMAGE, STICKER, EFFECT
}

enum class FilterType {
    BRIGHTNESS, CONTRAST, SATURATION, HUE, GAMMA, VIGNETTE, 
    BLUR, SHARPEN, VINTAGE, BLACK_WHITE, SEPIA, LUT, 
    CHROMA_KEY, BACKGROUND_REMOVAL
}

enum class EffectType {
    FADE_IN, FADE_OUT, ZOOM_IN, ZOOM_OUT, SLIDE_LEFT, 
    SLIDE_RIGHT, SLIDE_UP, SLIDE_DOWN, ROTATE, FLIP, 
    GLITCH, PARTICLE, LIGHT_LEAK, LENS_FLARE
}

enum class TransitionType {
    CUT, FADE, DISSOLVE, WIPE_LEFT, WIPE_RIGHT, WIPE_UP, 
    WIPE_DOWN, SLIDE_LEFT, SLIDE_RIGHT, ZOOM, ROTATE, 
    MORPH, PIXELATE, BLUR_TRANSITION
}

enum class InterpolationType {
    LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, BEZIER
}

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTrackList(tracks: List<Track>): String {
        return gson.toJson(tracks)
    }

    @TypeConverter
    fun toTrackList(tracksString: String): List<Track> {
        val listType = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(tracksString, listType)
    }

    @TypeConverter
    fun fromTrackItemList(items: List<TrackItem>): String {
        return gson.toJson(items)
    }

    @TypeConverter
    fun toTrackItemList(itemsString: String): List<TrackItem> {
        val listType = object : TypeToken<List<TrackItem>>() {}.type
        return gson.fromJson(itemsString, listType)
    }

    @TypeConverter
    fun fromFilterList(filters: List<Filter>): String {
        return gson.toJson(filters)
    }

    @TypeConverter
    fun toFilterList(filtersString: String): List<Filter> {
        val listType = object : TypeToken<List<Filter>>() {}.type
        return gson.fromJson(filtersString, listType)
    }

    @TypeConverter
    fun fromEffectList(effects: List<Effect>): String {
        return gson.toJson(effects)
    }

    @TypeConverter
    fun toEffectList(effectsString: String): List<Effect> {
        val listType = object : TypeToken<List<Effect>>() {}.type
        return gson.fromJson(effectsString, listType)
    }

    @TypeConverter
    fun fromTransitionList(transitions: List<Transition>): String {
        return gson.toJson(transitions)
    }

    @TypeConverter
    fun toTransitionList(transitionsString: String): List<Transition> {
        val listType = object : TypeToken<List<Transition>>() {}.type
        return gson.fromJson(transitionsString, listType)
    }

    @TypeConverter
    fun fromKeyframeList(keyframes: List<Keyframe>): String {
        return gson.toJson(keyframes)
    }

    @TypeConverter
    fun toKeyframeList(keyframesString: String): List<Keyframe> {
        val listType = object : TypeToken<List<Keyframe>>() {}.type
        return gson.fromJson(keyframesString, listType)
    }

    @TypeConverter
    fun fromParameterMap(parameters: Map<String, Any>): String {
        return gson.toJson(parameters)
    }

    @TypeConverter
    fun toParameterMap(parametersString: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(parametersString, mapType)
    }

    @TypeConverter
    fun fromAspectRatio(aspectRatio: AspectRatio): String {
        return gson.toJson(aspectRatio)
    }

    @TypeConverter
    fun toAspectRatio(aspectRatioString: String): AspectRatio {
        return gson.fromJson(aspectRatioString, AspectRatio::class.java)
    }

    @TypeConverter
    fun fromResolution(resolution: Resolution): String {
        return gson.toJson(resolution)
    }

    @TypeConverter
    fun toResolution(resolutionString: String): Resolution {
        return gson.fromJson(resolutionString, Resolution::class.java)
    }

    @TypeConverter
    fun fromTransform(transform: Transform): String {
        return gson.toJson(transform)
    }

    @TypeConverter
    fun toTransform(transformString: String): Transform {
        return gson.fromJson(transformString, Transform::class.java)
    }
}