package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val bannerColor: String = "#6366F1",
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val postsCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "username" to username,
            "displayName" to displayName,
            "bio" to bio,
            "avatarUrl" to avatarUrl,
            "bannerColor" to bannerColor,
            "followersCount" to followersCount,
            "followingCount" to followingCount,
            "postsCount" to postsCount,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class Post(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val authorAvatar: String = "",
    val content: String = "",
    val category: String = "General",
    val mediaType: String = "text", // "text", "image", "video"
    val mediaUrl: String = "",
    val videoDuration: String = "",
    val mediaColor: String = "",
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "authorName" to authorName,
            "authorHandle" to authorHandle,
            "authorAvatar" to authorAvatar,
            "content" to content,
            "category" to category,
            "mediaType" to mediaType,
            "mediaUrl" to mediaUrl,
            "videoDuration" to videoDuration,
            "mediaColor" to mediaColor,
            "likesCount" to likesCount,
            "commentsCount" to commentsCount,
            "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class PostLike(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Comment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Story(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val content: String = "",
    val mediaColor: String = "#6366F1",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FollowRecord(
    @DocumentId val id: String = "",
    val targetUserId: String = "",
    val userId: String = "",
    val targetUsername: String = "",
    val targetDisplayName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
