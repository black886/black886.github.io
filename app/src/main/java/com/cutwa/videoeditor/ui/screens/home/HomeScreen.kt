package com.cutwa.videoeditor.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cutwa.videoeditor.data.model.AspectRatio
import com.cutwa.videoeditor.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToProjects: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    )
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.createProjectFromVideo(it, context) }
    }
    
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    LaunchedEffect(uiState.newProjectId) {
        uiState.newProjectId?.let { projectId ->
            onNavigateToEditor(projectId)
            viewModel.onProjectOpened()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkGray, Black)
                )
            )
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cutwa",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            
            IconButton(onClick = onNavigateToProjects) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Projects",
                    tint = White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Start Section
        Text(
            text = "Quick Start",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(quickStartOptions) { option ->
                QuickStartCard(
                    option = option,
                    onClick = {
                        when (option.id) {
                            "import_video" -> videoPickerLauncher.launch("video/*")
                            "record_video" -> viewModel.startRecording(context)
                            "create_project" -> viewModel.createEmptyProject()
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Templates Section
        Text(
            text = "Templates",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(templateOptions) { template ->
                TemplateCard(
                    template = template,
                    onClick = {
                        viewModel.createProjectFromTemplate(template)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Recent Projects
        if (uiState.recentProjects.isNotEmpty()) {
            Text(
                text = "Recent Projects",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.recentProjects) { project ->
                    RecentProjectCard(
                        project = project,
                        onClick = { onNavigateToEditor(project.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStartCard(
    option: QuickStartOption,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = ToolBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                tint = option.color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = option.title,
                fontSize = 12.sp,
                color = White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateOption,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = ToolBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = template.gradientColors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = template.icon,
                    contentDescription = template.title,
                    tint = White,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = template.title,
                    fontSize = 14.sp,
                    color = White,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = template.aspectRatio.ratio,
                    fontSize = 12.sp,
                    color = White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun RecentProjectCard(
    project: com.cutwa.videoeditor.data.model.VideoProject,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = ToolBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = "Video thumbnail",
                    tint = White.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = project.name,
                    fontSize = 12.sp,
                    color = White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                
                Text(
                    text = formatDuration(project.duration),
                    fontSize = 10.sp,
                    color = White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

data class QuickStartOption(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

data class TemplateOption(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val aspectRatio: AspectRatio,
    val gradientColors: List<androidx.compose.ui.graphics.Color>
)

private val quickStartOptions = listOf(
    QuickStartOption("import_video", "Import Video", Icons.Default.VideoFile, AccentBlue),
    QuickStartOption("record_video", "Record Video", Icons.Default.Videocam, AccentRed),
    QuickStartOption("create_project", "New Project", Icons.Default.Add, AccentGreen)
)

private val templateOptions = listOf(
    TemplateOption(
        "tiktok", "TikTok", Icons.Default.PhoneAndroid, AspectRatio.RATIO_9_16,
        listOf(AccentPink, AccentPurple)
    ),
    TemplateOption(
        "youtube", "YouTube", Icons.Default.PlayArrow, AspectRatio.RATIO_16_9,
        listOf(AccentRed, AccentYellow)
    ),
    TemplateOption(
        "instagram", "Instagram", Icons.Default.CameraAlt, AspectRatio.RATIO_1_1,
        listOf(AccentPurple, AccentPink)
    ),
    TemplateOption(
        "widescreen", "Widescreen", Icons.Default.Tv, AspectRatio.RATIO_21_9,
        listOf(AccentBlue, AccentGreen)
    )
)