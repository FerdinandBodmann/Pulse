package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PulseRepository
import com.example.model.Comment
import com.example.model.Post
import com.example.model.Story
import com.example.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "PulseViewModel"
private const val STOP_TIMEOUT_MILLIS = 5000L

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class FeedTab { FOR_YOU, FOLLOWING }

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: PulseRepository,
    val currentUserId: String,
    initialDisplayName: String = "Pulse Member",
    initialEmail: String = ""
) : ViewModel() {

    // Current User Profile State
    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    // Feed Tab Filter (For You vs Following)
    val feedTab = MutableStateFlow(FeedTab.FOR_YOU)

    // Category Filter State
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // User's following list (defined earlier so it can be combined)
    val followingUserIds: StateFlow<Set<String>> = repository.observeFollowing(currentUserId)
        .catch { error ->
            Log.w(TAG, "Error observing following users", error)
            emit(emptySet())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptySet()
        )

    // Main Feed Posts with Following filter support
    val feedPosts: StateFlow<UiState<List<Post>>> = kotlinx.coroutines.flow.combine(
        _selectedCategory.flatMapLatest { category ->
            repository.observePosts(if (category == "All") null else category)
        },
        followingUserIds,
        feedTab
    ) { posts, following, tab ->
        when (tab) {
            FeedTab.FOR_YOU -> posts
            FeedTab.FOLLOWING -> posts.filter { it.userId in following || it.userId == currentUserId }
        }
    }
        .map<List<Post>, UiState<List<Post>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing feed posts", error)
            emit(UiState.Error(error.message ?: "Failed to load feed"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // Stories Flow
    val stories: StateFlow<UiState<List<Story>>> = repository.observeStories()
        .map<List<Story>, UiState<List<Story>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing stories", error)
            emit(UiState.Error(error.message ?: "Failed to load stories"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // Current User Posts
    val userPosts: StateFlow<UiState<List<Post>>> = repository.observeUserPosts(currentUserId)
        .map<List<Post>, UiState<List<Post>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing user posts", error)
            emit(UiState.Error(error.message ?: "Failed to load posts"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // User's liked post IDs
    val likedPostIds: StateFlow<Set<String>> = repository.observeUserLikedPosts(currentUserId)
        .catch { error ->
            Log.w(TAG, "Error observing user likes", error)
            emit(emptySet())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptySet()
        )



    // Comments Modal State
    private val _activePostForComments = MutableStateFlow<Post?>(null)
    val activePostForComments = _activePostForComments.asStateFlow()

    val activeComments: StateFlow<UiState<List<Comment>>> = _activePostForComments
        .flatMapLatest { post ->
            if (post != null) {
                repository.observeComments(post.id)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .map<List<Comment>, UiState<List<Comment>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing comments", error)
            emit(UiState.Error(error.message ?: "Failed to load comments"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // Active Story Viewer State
    private val _activeStory = MutableStateFlow<Story?>(null)
    val activeStory = _activeStory.asStateFlow()

    // Search & Explore State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults: StateFlow<UiState<List<UserProfile>>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchUsers(query)
        }
        .map<List<UserProfile>, UiState<List<UserProfile>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error searching users", error)
            emit(UiState.Error(error.message ?: "Search failed"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // Transient UI message / toast
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    init {
        // Auto-bootstrap profile if missing
        viewModelScope.launch {
            val emailPrefix = initialEmail.substringBefore("@").replace(".", "").ifBlank { "user" }
            val defaultHandle = emailPrefix.take(20).lowercase()
            repository.saveUserProfile(
                UserProfile(
                    userId = currentUserId,
                    username = defaultHandle,
                    displayName = initialDisplayName.ifBlank { "Pulse Creator" },
                    bio = "Connecting on Pulse ⚡",
                    bannerColor = "#6366F1"
                )
            )
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openComments(post: Post) {
        _activePostForComments.value = post
    }

    fun closeComments() {
        _activePostForComments.value = null
    }

    fun openStory(story: Story) {
        _activeStory.value = story
    }

    fun closeStory() {
        _activeStory.value = null
    }

    fun setFeedTab(tab: FeedTab) {
        feedTab.value = tab
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            val isLiked = likedPostIds.value.contains(post.id)
            repository.toggleLike(post.id, isLiked)
                .onFailure {
                    _userMessage.value = "Failed to update like: ${it.localizedMessage}"
                }
        }
    }

    fun createPost(
        content: String,
        category: String,
        mediaColor: String,
        mediaType: String = "text",
        mediaUrl: String = "",
        videoDuration: String = ""
    ) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val profile = userProfile.value
            val authorName = profile?.displayName?.ifBlank { "Pulse Creator" } ?: "Pulse Creator"
            val authorHandle = profile?.username?.ifBlank { "creator" } ?: "creator"
            val authorAvatar = profile?.avatarUrl ?: ""

            repository.createPost(
                content = content.trim(),
                category = category,
                mediaColor = mediaColor,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatar = authorAvatar,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                videoDuration = videoDuration
            ).onSuccess {
                _userMessage.value = "Post shared on Pulse!"
            }.onFailure {
                _userMessage.value = "Failed to create post: ${it.localizedMessage}"
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
                .onSuccess {
                    _userMessage.value = "Post deleted"
                }
                .onFailure {
                    _userMessage.value = "Failed to delete post: ${it.localizedMessage}"
                }
        }
    }

    fun addComment(text: String) {
        val post = _activePostForComments.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            val profile = userProfile.value
            val authorName = profile?.displayName?.ifBlank { "Pulse Creator" } ?: "Pulse Creator"
            val authorAvatar = profile?.avatarUrl ?: ""

            repository.addComment(
                postId = post.id,
                text = text.trim(),
                authorName = authorName,
                authorAvatar = authorAvatar
            ).onSuccess {
                // Comment added
            }.onFailure {
                _userMessage.value = "Failed to post comment: ${it.localizedMessage}"
            }
        }
    }

    fun createStory(content: String, mediaColor: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val profile = userProfile.value
            val authorName = profile?.displayName?.ifBlank { "Pulse Creator" } ?: "Pulse Creator"
            val authorAvatar = profile?.avatarUrl ?: ""

            repository.createStory(
                content = content.trim(),
                mediaColor = mediaColor,
                authorName = authorName,
                authorAvatar = authorAvatar
            ).onSuccess {
                _userMessage.value = "Story shared for 24 hours!"
            }.onFailure {
                _userMessage.value = "Failed to post story: ${it.localizedMessage}"
            }
        }
    }

    fun toggleFollow(user: UserProfile) {
        viewModelScope.launch {
            val isFollowing = followingUserIds.value.contains(user.userId)
            repository.toggleFollow(
                targetUserId = user.userId,
                targetUsername = user.username,
                targetDisplayName = user.displayName,
                currentlyFollowing = isFollowing
            ).onSuccess {
                _userMessage.value = if (isFollowing) "Unfollowed @${user.username}" else "Following @${user.username}"
            }.onFailure {
                _userMessage.value = "Action failed: ${it.localizedMessage}"
            }
        }
    }

    fun updateUserProfile(
        username: String,
        displayName: String,
        bio: String,
        avatarUrl: String,
        bannerColor: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = (current ?: UserProfile(userId = currentUserId)).copy(
                username = username.trim().lowercase(),
                displayName = displayName.trim(),
                bio = bio.trim(),
                avatarUrl = avatarUrl.trim(),
                bannerColor = bannerColor
            )
            repository.saveUserProfile(updated)
                .onSuccess {
                    _userMessage.value = "Profile updated!"
                }
                .onFailure {
                    _userMessage.value = "Failed to update profile: ${it.localizedMessage}"
                }
        }
    }
}
