package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PulseCoralAccent
import com.example.ui.theme.PulseCyanLight
import com.example.ui.theme.PulseVioletLight
import com.example.ui.theme.PulseVioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (content: String, category: String, mediaColor: String, mediaType: String, videoDuration: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var textContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tech") }
    var selectedMediaType by remember { mutableStateOf("text") } // "text", "image", "video"
    var videoDuration by remember { mutableStateOf("0:30") }
    var selectedColor by remember { mutableStateOf("#6366F1") }

    val categories = listOf("Tech", "Life", "Art", "Video", "Music", "Fitness", "General")
    val colorOptions = listOf("#6366F1", "#06B6D4", "#F43F5E", "#10B981", "#8B5CF6", "#F59E0B")
    val videoDurations = listOf("0:15", "0:30", "0:60")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("create_post_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Post",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_create_post_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Text Area
            OutlinedTextField(
                value = textContent,
                onValueChange = { if (it.length <= 500) textContent = it },
                placeholder = {
                    Text("What's on your mind? Share thoughts, links, or media...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("post_content_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PulseVioletPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // Character counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${textContent.length}/500",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Media Type Picker: Text vs Photo vs Video
            Text(
                text = "Attach Media",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MediaTypeButton(
                    label = "Text Only",
                    icon = Icons.Default.TextFields,
                    isSelected = selectedMediaType == "text",
                    onClick = { selectedMediaType = "text" },
                    modifier = Modifier.weight(1f).testTag("media_type_text")
                )
                MediaTypeButton(
                    label = "Photo Card",
                    icon = Icons.Default.Image,
                    isSelected = selectedMediaType == "image",
                    onClick = { selectedMediaType = "image" },
                    modifier = Modifier.weight(1f).testTag("media_type_image")
                )
                MediaTypeButton(
                    label = "Short Video",
                    icon = Icons.Default.Videocam,
                    isSelected = selectedMediaType == "video",
                    onClick = { selectedMediaType = "video" },
                    modifier = Modifier.weight(1f).testTag("media_type_video")
                )
            }

            // If video selected, show duration selector
            if (selectedMediaType == "video") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Clip Length:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    videoDurations.forEach { dur ->
                        FilterChip(
                            selected = videoDuration == dur,
                            onClick = { videoDuration = dur },
                            label = { Text(dur, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseCoralAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Picker
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PulseVioletPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Accent Color Picker for Card
            if (selectedMediaType != "text") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex, Color.Gray))
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Post Button
            Button(
                onClick = {
                    if (textContent.isNotBlank()) {
                        onSubmit(
                            textContent,
                            selectedCategory,
                            selectedColor,
                            selectedMediaType,
                            if (selectedMediaType == "video") videoDuration else ""
                        )
                        onDismiss()
                    }
                },
                enabled = textContent.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_post_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseVioletPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Publish to Pulse",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MediaTypeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) PulseVioletPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    val border = if (isSelected) PulseVioletPrimary else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PulseVioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PulseVioletPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
