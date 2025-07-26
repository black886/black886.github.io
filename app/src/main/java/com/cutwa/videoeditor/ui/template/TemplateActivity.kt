package com.cutwa.videoeditor.ui.template

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cutwa.videoeditor.ui.editor.VideoEditorActivity
import com.cutwa.videoeditor.ui.theme.CutwaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TemplateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CutwaTheme {
                TemplateScreen(
                    onNavigateBack = { finish() },
                    onTemplateSelected = { template ->
                        val intent = Intent(this, VideoEditorActivity::class.java)
                        intent.putExtra("template", template)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateSelected: (VideoTemplate) -> Unit
) {
    val templates = remember { getVideoTemplates() }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All", "Social Media", "Business", "Travel", "Food", "Fashion", "Music")
    
    val filteredTemplates = if (selectedCategory == "All") {
        templates
    } else {
        templates.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Templates",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category filter
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Templates grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTemplates) { template ->
                    TemplateCard(
                        template = template,
                        onSelected = { onTemplateSelected(template) }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: VideoTemplate,
    onSelected: () -> Unit
) {
    Card(
        onClick = onSelected,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Template preview image
            AsyncImage(
                model = template.previewImageUrl,
                contentDescription = template.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Template info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = template.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = template.duration,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    if (template.isPremium) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFD700)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "PRO",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.9f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Preview",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

data class VideoTemplate(
    val id: String,
    val name: String,
    val category: String,
    val duration: String,
    val previewImageUrl: String,
    val previewVideoUrl: String,
    val isPremium: Boolean = false,
    val aspectRatio: String = "9:16",
    val tags: List<String> = emptyList()
)

fun getVideoTemplates(): List<VideoTemplate> {
    return listOf(
        VideoTemplate(
            id = "1",
            name = "Instagram Story",
            category = "Social Media",
            duration = "15s",
            previewImageUrl = "https://via.placeholder.com/300x400",
            previewVideoUrl = "",
            aspectRatio = "9:16",
            tags = listOf("instagram", "story", "social")
        ),
        VideoTemplate(
            id = "2",
            name = "TikTok Trending",
            category = "Social Media",
            duration = "30s",
            previewImageUrl = "https://via.placeholder.com/300x400",
            previewVideoUrl = "",
            aspectRatio = "9:16",
            tags = listOf("tiktok", "trending", "viral")
        ),
        VideoTemplate(
            id = "3",
            name = "Product Showcase",
            category = "Business",
            duration = "45s",
            previewImageUrl = "https://via.placeholder.com/300x400",
            previewVideoUrl = "",
            isPremium = true,
            aspectRatio = "16:9",
            tags = listOf("product", "business", "showcase")
        ),
        VideoTemplate(
            id = "4",
            name = "Travel Vlog",
            category = "Travel",
            duration = "60s",
            previewImageUrl = "https://via.placeholder.com/300x400",
            previewVideoUrl = "",
            aspectRatio = "16:9",
            tags = listOf("travel", "vlog", "adventure")
        ),
        VideoTemplate(
            id = "5",
            name = "Food Recipe",
            category = "Food",
            duration = "30s",
            previewImageUrl = "https://via.placeholder.com/300x400",
            previewVideoUrl = "",
            aspectRatio = "1:1",
            tags = listOf("food", "recipe", "cooking")
        ),
        VideoTemplate(
            id = "6",
            name = "Fashion Lookbook",
            category = "Fashion",
            duration = "45s",
            previewImageUrl = "https://via.placeholder.com/300x400",
            previewVideoUrl = "",
            isPremium = true,
            aspectRatio = "9:16",
            tags = listOf("fashion", "style", "lookbook")
        ),
        VideoTemplate(
            id = "7",
            name = "Music Visualizer",
            category = "Music",
            duration = "3m",
            previewImageUrl = "https://via.placeholder.com/300x400",
            previewVideoUrl = "",
            aspectRatio = "16:9",
            tags = listOf("music", "visualizer", "audio")
        ),
        VideoTemplate(
            id = "8",
            name = "YouTube Intro",
            category = "Business",
            duration = "10s",
            previewImageUrl = "https://via.placeholder.com/300x400",
            previewVideoUrl = "",
            aspectRatio = "16:9",
            tags = listOf("youtube", "intro", "branding")
        )
    )
}