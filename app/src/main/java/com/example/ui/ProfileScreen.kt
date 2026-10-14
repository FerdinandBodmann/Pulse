package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PulseCoralAccent
import com.example.ui.theme.PulseCyanLight
import com.example.ui.theme.PulseVioletLight
import com.example.ui.theme.PulseVioletPrimary

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val userPostsState by viewModel.userPosts.collectAsStateWithLifecycle()
    val likedPostIds by viewModel.likedPostIds.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var selectedProfileTab by remember { mutableIntStateOf(0) }

    val user = profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banner and Avatar Stack
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Banner Background
            val bannerBrush = Brush.linearGradient(
                listOf(
                    parseHexColor(user?.bannerColor ?: "#6366F1", PulseVioletPrimary),
                    Color(0xFF1E1B4B)
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(bannerBrush)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { showSignOutConfirm = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Avatar overlapping banner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp)
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(PulseVioletPrimary, PulseCyanLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (user?.displayName ?: "P").take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                )
            }

            // Edit Profile Button
            OutlinedButton(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 4.dp)
                    .testTag("edit_profile_button"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Profile", fontSize = 13.sp)
            }
        }

        // User Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = user?.displayName?.ifBlank { "Pulse Creator" } ?: "Pulse Creator",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "@${user?.username?.ifBlank { "creator" } ?: "creator"}",
                style = MaterialTheme.typography.bodyMedium,
                color = PulseVioletLight
            )

            if (!user?.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = user?.bio ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Followers, Following, Posts Count Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Followers",
                    count = "${user?.followersCount ?: 0}",
                    modifier = Modifier.weight(1f).testTag("stat_followers")
                )
                StatCard(
                    label = "Following",
                    count = "${user?.followingCount ?: 0}",
                    modifier = Modifier.weight(1f).testTag("stat_following")
                )
                StatCard(
                    label = "Posts",
                    count = "${user?.postsCount ?: 0}",
                    modifier = Modifier.weight(1f).testTag("stat_posts")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs: My Posts
        TabRow(
            selectedTabIndex = selectedProfileTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PulseVioletPrimary
        ) {
            Tab(
                selected = selectedProfileTab == 0,
                onClick = { selectedProfileTab = 0 },
                text = { Text("My Posts", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedProfileTab == 1,
                onClick = { selectedProfileTab = 1 },
                text = { Text("Liked Posts", fontWeight = FontWeight.Bold) }
            )
        }

        // Posts List
        when (val state = userPostsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PulseVioletPrimary)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            is UiState.Success -> {
                val posts = if (selectedProfileTab == 0) {
                    state.data
                } else {
                    state.data.filter { likedPostIds.contains(it.id) }
                }

                if (posts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedProfileTab == 0) "You haven't posted yet" else "No liked posts yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(posts, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                isLiked = likedPostIds.contains(post.id),
                                isOwner = true,
                                onLike = { viewModel.toggleLike(post) },
                                onComment = { viewModel.openComments(post) },
                                onDelete = { viewModel.deletePost(post.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            currentDisplayName = user?.displayName ?: "",
            currentUsername = user?.username ?: "",
            currentBio = user?.bio ?: "",
            currentBannerColor = user?.bannerColor ?: "#6366F1",
            onDismiss = { showEditDialog = false },
            onSave = { displayName, username, bio, bannerColor ->
                viewModel.updateUserProfile(username, displayName, bio, user?.avatarUrl ?: "", bannerColor)
                showEditDialog = false
            }
        )
    }

    // Sign Out Confirmation
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign Out of Pulse?") },
            text = { Text("You can sign back in at any time with your Google Account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulseCoralAccent)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    count: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentDisplayName: String,
    currentUsername: String,
    currentBio: String,
    currentBannerColor: String,
    onDismiss: () -> Unit,
    onSave: (displayName: String, username: String, bio: String, bannerColor: String) -> Unit
) {
    var displayName by remember { mutableStateOf(currentDisplayName) }
    var username by remember { mutableStateOf(currentUsername) }
    var bio by remember { mutableStateOf(currentBio) }
    var bannerColor by remember { mutableStateOf(currentBannerColor) }

    val colors = listOf("#6366F1", "#06B6D4", "#F43F5E", "#10B981", "#8B5CF6", "#F59E0B")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_display_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        val sanitized = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' }
                        if (sanitized.length <= 30) username = sanitized
                    },
                    label = { Text("Username (Handle)") },
                    prefix = { Text("@") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_username_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 250) bio = it },
                    label = { Text("Short Bio") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_bio_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Text(
                    text = "Banner Accent Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { hex ->
                        val isSelected = bannerColor == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex, Color.Gray))
                                .clickable { bannerColor = hex }
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (displayName.isNotBlank() && username.isNotBlank()) {
                        onSave(displayName, username, bio, bannerColor)
                    }
                },
                enabled = displayName.isNotBlank() && username.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PulseVioletPrimary),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
