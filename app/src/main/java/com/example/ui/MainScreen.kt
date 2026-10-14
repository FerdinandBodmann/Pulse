package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

enum class PulseScreen {
    FEED,
    EXPLORE,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(PulseScreen.FEED) }
    var showCreatePost by remember { mutableStateOf(false) }
    var showCreateStory by remember { mutableStateOf(false) }

    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val activeStory by viewModel.activeStory.collectAsStateWithLifecycle()
    val activePostForComments by viewModel.activePostForComments.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        val msg = userMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(PulseCyanLight, PulseVioletLight, PulseCoralAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pulse",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                NavigationBarItem(
                    selected = currentScreen == PulseScreen.FEED,
                    onClick = { currentScreen = PulseScreen.FEED },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == PulseScreen.FEED) Icons.Filled.DynamicFeed else Icons.Outlined.DynamicFeed,
                            contentDescription = "Feed"
                        )
                    },
                    label = { Text("Feed") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PulseVioletPrimary,
                        selectedTextColor = PulseVioletPrimary,
                        indicatorColor = PulseVioletPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_feed")
                )

                NavigationBarItem(
                    selected = currentScreen == PulseScreen.EXPLORE,
                    onClick = { currentScreen = PulseScreen.EXPLORE },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == PulseScreen.EXPLORE) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = "Explore"
                        )
                    },
                    label = { Text("Explore") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PulseVioletPrimary,
                        selectedTextColor = PulseVioletPrimary,
                        indicatorColor = PulseVioletPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_explore")
                )

                NavigationBarItem(
                    selected = currentScreen == PulseScreen.PROFILE,
                    onClick = { currentScreen = PulseScreen.PROFILE },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == PulseScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PulseVioletPrimary,
                        selectedTextColor = PulseVioletPrimary,
                        indicatorColor = PulseVioletPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(visible = currentScreen == PulseScreen.FEED) {
                FloatingActionButton(
                    onClick = { showCreatePost = true },
                    containerColor = PulseVioletPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_create_post")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Post",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                PulseScreen.FEED -> FeedScreen(
                    viewModel = viewModel,
                    onOpenCreatePost = { showCreatePost = true },
                    onOpenCreateStory = { showCreateStory = true },
                    onNavigateToExplore = { currentScreen = PulseScreen.EXPLORE }
                )
                PulseScreen.EXPLORE -> ExploreScreen(
                    viewModel = viewModel,
                    onSelectTag = { tag ->
                        viewModel.selectCategory(tag)
                        currentScreen = PulseScreen.FEED
                    }
                )
                PulseScreen.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onSignOut = onSignOut
                )
            }

            // Create Post Dialog
            if (showCreatePost) {
                CreatePostDialog(
                    onDismiss = { showCreatePost = false },
                    onSubmit = { content, category, color, mediaType, videoDuration ->
                        viewModel.createPost(content, category, color, mediaType, "", videoDuration)
                    }
                )
            }

            // Create Story Dialog
            if (showCreateStory) {
                CreateStoryDialog(
                    onDismiss = { showCreateStory = false },
                    onSubmit = { content, color ->
                        viewModel.createStory(content, color)
                    }
                )
            }

            // Comments Bottom Sheet
            activePostForComments?.let { post ->
                CommentsSheet(
                    post = post,
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeComments() }
                )
            }

            // Story Viewer Overlay
            activeStory?.let { story ->
                StoryViewerDialog(
                    story = story,
                    onDismiss = { viewModel.closeStory() }
                )
            }
        }
    }
}
