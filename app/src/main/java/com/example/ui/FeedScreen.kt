package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Post
import com.example.model.Story
import com.example.ui.theme.PulseCoralAccent
import com.example.ui.theme.PulseCyanLight
import com.example.ui.theme.PulseVioletLight
import com.example.ui.theme.PulseVioletPrimary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FeedScreen(
    viewModel: MainViewModel,
    onOpenCreatePost: () -> Unit,
    onOpenCreateStory: () -> Unit,
    onNavigateToExplore: () -> Unit
) {
    val feedPostsState by viewModel.feedPosts.collectAsStateWithLifecycle()
    val storiesState by viewModel.stories.collectAsStateWithLifecycle()
    val likedPostIds by viewModel.likedPostIds.collectAsStateWithLifecycle()
    val currentTab by viewModel.feedTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val categories = listOf("All", "Tech", "Life", "Art", "Video", "Music", "Fitness")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row: For You vs Following
        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                    color = PulseVioletPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = currentTab == FeedTab.FOR_YOU,
                onClick = { viewModel.setFeedTab(FeedTab.FOR_YOU) },
                text = {
                    Text(
                        "For You",
                        fontWeight = if (currentTab == FeedTab.FOR_YOU) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.testTag("tab_for_you")
            )
            Tab(
                selected = currentTab == FeedTab.FOLLOWING,
                onClick = { viewModel.setFeedTab(FeedTab.FOLLOWING) },
                text = {
                    Text(
                        "Following",
                        fontWeight = if (currentTab == FeedTab.FOLLOWING) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.testTag("tab_following")
            )
        }

        // Stories Bar
        StoriesTray(
            storiesState = storiesState,
            onAddStory = onOpenCreateStory,
            onSelectStory = { viewModel.openStory(it) }
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category, fontSize = 13.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PulseVioletPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }

        // Feed Posts Stream
        when (val state = feedPostsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PulseVioletPrimary)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Could not load posts",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is UiState.Success -> {
                val posts = state.data
                if (posts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (currentTab == FeedTab.FOLLOWING) "No posts from following yet" else "No posts in this category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (currentTab == FeedTab.FOLLOWING)
                                    "Follow interesting creators in Explore to see their latest posts here!"
                                else
                                    "Be the first one to share a post or moment with the community!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (currentTab == FeedTab.FOLLOWING) {
                                androidx.compose.material3.FilledTonalButton(
                                    onClick = onNavigateToExplore,
                                    modifier = Modifier.testTag("empty_explore_button")
                                ) {
                                    Text("Discover Creators")
                                }
                            } else {
                                androidx.compose.material3.Button(
                                    onClick = onOpenCreatePost,
                                    modifier = Modifier.testTag("empty_create_post_button")
                                ) {
                                    Text("Create Post")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(posts, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                isLiked = likedPostIds.contains(post.id),
                                isOwner = post.userId == viewModel.currentUserId,
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
}

@Composable
fun StoriesTray(
    storiesState: UiState<List<Story>>,
    onAddStory: () -> Unit,
    onSelectStory: (Story) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Add Story" Button
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onAddStory() }
                    .testTag("add_story_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, PulseVioletPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Post Story",
                        tint = PulseVioletPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Story",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }

        if (storiesState is UiState.Success) {
            items(storiesState.data, key = { it.id }) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectStory(story) }
                        .testTag("story_item_${story.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .border(
                                2.5.dp,
                                Brush.sweepGradient(listOf(PulseCyanLight, PulseVioletLight, PulseCoralAccent)),
                                CircleShape
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(story.mediaColor, Color(0xFF6366F1))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = story.authorName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.authorName.take(8),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    isLiked: Boolean,
    isOwner: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onDelete: () -> Unit
) {
    val heartColor by animateColorAsState(
        targetValue = if (isLiked) PulseCoralAccent else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(),
        label = "heart_anim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author Avatar & Names
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PulseVioletPrimary, PulseCyanLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "@${post.authorHandle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formatRelativeTimestamp(post.createdAt?.toDate()?.time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PulseVioletPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PulseVioletPrimary
                    )
                }

                if (isOwner) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_post_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Post",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            // Optional Media Preview (Photo or Video Clip)
            if (post.mediaType == "video" || post.mediaType == "image") {
                Spacer(modifier = Modifier.height(12.dp))
                val bgBrush = if (post.mediaColor.isNotBlank()) {
                    Brush.linearGradient(
                        listOf(
                            parseHexColor(post.mediaColor, Color(0xFF312E81)),
                            Color(0xFF1E1B4B)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color(0xFF312E81), Color(0xFF1E1B4B)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgBrush),
                    contentAlignment = Alignment.Center
                ) {
                    if (post.mediaType == "video") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video Clip",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Short Video • ${post.videoDuration.ifBlank { "0:30" }}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    } else {
                        // Image attachment card
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Media Attachment",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Likes & Comments
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLike() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Post",
                        tint = heartColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = heartColor
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onComment() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Share Button
                IconButton(
                    onClick = { /* Share */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatRelativeTimestamp(epochMillis: Long?): String {
    if (epochMillis == null) return "Just now"
    val diffSec = (System.currentTimeMillis() - epochMillis) / 1000
    return when {
        diffSec < 60 -> "Just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        diffSec < 604800 -> "${diffSec / 86400}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(epochMillis)
    }
}

fun parseHexColor(hex: String, fallback: Color): Color {
    return try {
        if (hex.startsWith("#") && (hex.length == 7 || hex.length == 9)) {
            Color(android.graphics.Color.parseColor(hex))
        } else fallback
    } catch (e: Exception) {
        fallback
    }
}
