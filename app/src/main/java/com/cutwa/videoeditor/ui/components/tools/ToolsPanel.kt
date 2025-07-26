package com.cutwa.videoeditor.ui.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cutwa.videoeditor.data.model.*
import com.cutwa.videoeditor.ui.screens.editor.EditorTool
import com.cutwa.videoeditor.ui.theme.*

@Composable
fun ToolsPanel(
    modifier: Modifier = Modifier,
    selectedTool: EditorTool,
    selectedClip: TimelineClip?,
    onToolSelected: (EditorTool) -> Unit,
    onEffectApplied: (Effect) -> Unit,
    onFilterApplied: (Filter) -> Unit,
    onTextAdded: (String) -> Unit,
    onStickerAdded: (String) -> Unit
) {
    Column(
        modifier = modifier.background(ToolBackground)
    ) {
        // Tool selector tabs
        ToolSelector(
            selectedTool = selectedTool,
            onToolSelected = onToolSelected
        )
        
        Divider(color = White.copy(alpha = 0.2f))
        
        // Tool content based on selected tool
        when (selectedTool) {
            EditorTool.EFFECTS -> EffectsPanel(onEffectApplied = onEffectApplied)
            EditorTool.FILTERS -> FiltersPanel(onFilterApplied = onFilterApplied)
            EditorTool.TEXT -> TextPanel(onTextAdded = onTextAdded)
            EditorTool.STICKERS -> StickersPanel(onStickerAdded = onStickerAdded)
            EditorTool.AUDIO -> AudioPanel()
            EditorTool.TRANSITIONS -> TransitionsPanel()
            else -> SelectPanel(selectedClip = selectedClip)
        }
    }
}

@Composable
private fun ToolSelector(
    selectedTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(toolItems) { tool ->
            ToolTab(
                tool = tool,
                isSelected = selectedTool == tool.editorTool,
                onClick = { onToolSelected(tool.editorTool) }
            )
        }
    }
}

@Composable
private fun ToolTab(
    tool: ToolItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ToolSelected else ToolBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.name,
                tint = ToolIcon,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = tool.name,
                color = White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EffectsPanel(onEffectApplied: (Effect) -> Unit) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(effectsList) { effect ->
            EffectCard(
                effect = effect,
                onClick = { onEffectApplied(effect) }
            )
        }
    }
}

@Composable
private fun FiltersPanel(onFilterApplied: (Filter) -> Unit) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filtersList) { filter ->
            FilterCard(
                filter = filter,
                onClick = { onFilterApplied(filter) }
            )
        }
    }
}

@Composable
private fun TextPanel(onTextAdded: (String) -> Unit) {
    var textInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = White,
                unfocusedTextColor = White,
                focusedLabelColor = AccentBlue,
                unfocusedLabelColor = White.copy(alpha = 0.7f)
            )
        )
        
        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    onTextAdded(textInput)
                    textInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue
            )
        ) {
            Text("Add Text", color = White)
        }
    }
}

@Composable
private fun StickersPanel(onStickerAdded: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stickersList) { sticker ->
            StickerCard(
                sticker = sticker,
                onClick = { onStickerAdded(sticker) }
            )
        }
    }
}

@Composable
private fun AudioPanel() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Audio Tools",
            color = White,
            fontSize = 16.sp
        )
        // Add audio editing controls here
    }
}

@Composable
private fun TransitionsPanel() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Transitions",
            color = White,
            fontSize = 16.sp
        )
        // Add transition controls here
    }
}

@Composable
private fun SelectPanel(selectedClip: TimelineClip?) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        if (selectedClip != null) {
            Text(
                text = "Selected Clip",
                color = White,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Type: ${when (selectedClip) {
                    is TimelineClip.VideoClip -> "Video"
                    is TimelineClip.AudioClip -> "Audio"
                    is TimelineClip.TextClip -> "Text"
                    is TimelineClip.ImageClip -> "Image"
                }}",
                color = White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        } else {
            Text(
                text = "Select a clip to edit",
                color = White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EffectCard(
    effect: Effect,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        )
    ) {
        Text(
            text = effect.type.name,
            color = White,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun FilterCard(
    filter: Filter,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        )
    ) {
        Text(
            text = filter.type.name,
            color = White,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun StickerCard(
    sticker: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        )
    ) {
        Text(
            text = sticker,
            color = White,
            modifier = Modifier.padding(12.dp)
        )
    }
}

data class ToolItem(
    val editorTool: EditorTool,
    val name: String,
    val icon: ImageVector
)

private val toolItems = listOf(
    ToolItem(EditorTool.SELECT, "Select", Icons.Default.CenterFocusWeak),
    ToolItem(EditorTool.CUT, "Cut", Icons.Default.ContentCut),
    ToolItem(EditorTool.EFFECTS, "Effects", Icons.Default.AutoFixHigh),
    ToolItem(EditorTool.FILTERS, "Filters", Icons.Default.Tune),
    ToolItem(EditorTool.TEXT, "Text", Icons.Default.TextFields),
    ToolItem(EditorTool.STICKERS, "Stickers", Icons.Default.EmojiEmotions),
    ToolItem(EditorTool.AUDIO, "Audio", Icons.Default.VolumeUp),
    ToolItem(EditorTool.TRANSITIONS, "Transitions", Icons.Default.CompareArrows)
)

private val effectsList = listOf(
    Effect("blur", EffectType.BLUR, 0.5f),
    Effect("vintage", EffectType.VINTAGE, 1.0f),
    Effect("neon", EffectType.NEON, 0.8f),
    Effect("glitch", EffectType.GLITCH, 0.6f)
)

private val filtersList = listOf(
    Filter("brightness", FilterType.BRIGHTNESS, 0.2f),
    Filter("contrast", FilterType.CONTRAST, 0.3f),
    Filter("saturation", FilterType.SATURATION, 0.5f),
    Filter("hue", FilterType.HUE, 0.1f)
)

private val stickersList = listOf(
    "😀", "😍", "🎉", "🔥", "⭐", "💯", "👍", "❤️"
)