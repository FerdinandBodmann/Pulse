package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Comment
import com.example.model.FollowRecord
import com.example.model.Post
import com.example.model.PostLike
import com.example.model.Story
import com.example.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PulseRepository(private val db: FirebaseFirestore) {

    // Convenience constructor resolving the provisioned custom database ID
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile Operations ---
    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else null
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                throw error
            }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val payload = mapOf(
                "userId" to uid,
                "username" to profile.username,
                "displayName" to profile.displayName,
                "bio" to profile.bio,
                "avatarUrl" to profile.avatarUrl,
                "bannerColor" to profile.bannerColor,
                "followersCount" to profile.followersCount,
                "followingCount" to profile.followingCount,
                "postsCount" to profile.postsCount,
                "createdAt" to (profile.createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserProfile(
        username: String,
        displayName: String,
        bio: String,
        avatarUrl: String = "",
        bannerColor: String = ""
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val updates = mutableMapOf<String, Any>(
                "username" to username.trim().lowercase(),
                "displayName" to displayName.trim(),
                "bio" to bio.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (avatarUrl.isNotBlank()) updates["avatarUrl"] = avatarUrl.trim()
            if (bannerColor.isNotBlank()) updates["bannerColor"] = bannerColor
            docRef.update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- Posts Operations ---
    fun observePosts(category: String? = null): Flow<List<Post>> {
        val query: Query = if (!category.isNullOrBlank() && category != "All" && category != "For You" && category != "Trending") {
            db.collection("posts")
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
        } else {
            db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
        }

        return query.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "posts")
                throw error
            }
    }

    fun observeUserPosts(userId: String): Flow<List<Post>> {
        return db.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "posts")
                throw error
            }
    }

    suspend fun createPost(
        content: String,
        category: String,
        mediaColor: String,
        authorName: String,
        authorHandle: String,
        authorAvatar: String,
        mediaType: String = "text",
        mediaUrl: String = "",
        videoDuration: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val postsCol = db.collection("posts")
        val payload = mapOf(
            "userId" to uid,
            "authorName" to authorName,
            "authorHandle" to authorHandle,
            "authorAvatar" to authorAvatar,
            "content" to content,
            "category" to category,
            "mediaType" to mediaType,
            "mediaUrl" to mediaUrl,
            "videoDuration" to videoDuration,
            "mediaColor" to mediaColor,
            "likesCount" to 0L,
            "commentsCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            val docRef = postsCol.add(payload).await()
            // Increment user's post count
            db.collection("users").document(uid).update(
                "postsCount", FieldValue.increment(1),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, postsCol.path)
            throw e
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("posts").document(postId)
        try {
            docRef.delete().await()
            // Decrement user's post count
            db.collection("users").document(uid).update(
                "postsCount", FieldValue.increment(-1),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Likes Operations ---
    suspend fun toggleLike(postId: String, currentlyLiked: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val likeDoc = db.collection("posts").document(postId).collection("likes").document(uid)
        val postDoc = db.collection("posts").document(postId)

        try {
            if (currentlyLiked) {
                likeDoc.delete().await()
                postDoc.update(
                    "likesCount", FieldValue.increment(-1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
            } else {
                likeDoc.set(mapOf(
                    "userId" to uid,
                    "postId" to postId,
                    "createdAt" to FieldValue.serverTimestamp()
                )).await()
                postDoc.update(
                    "likesCount", FieldValue.increment(1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, likeDoc.path)
            throw e
        }
    }

    fun observeUserLikedPosts(userId: String): Flow<Set<String>> {
        // Observers can check their own liked post IDs
        return db.collectionGroup("likes")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("postId") }.toSet()
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "likes")
                throw error
            }
    }

    fun isPostLiked(postId: String, userId: String): Flow<Boolean> {
        val docRef = db.collection("posts").document(postId).collection("likes").document(userId)
        return docRef.snapshots()
            .map { it.exists() }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                emit(false)
            }
    }

    // --- Comments Operations ---
    fun observeComments(postId: String): Flow<List<Comment>> {
        val path = "posts/$postId/comments"
        return db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Comment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addComment(
        postId: String,
        text: String,
        authorName: String,
        authorAvatar: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val commentsCol = db.collection("posts").document(postId).collection("comments")
        val postDoc = db.collection("posts").document(postId)

        val payload = mapOf(
            "postId" to postId,
            "userId" to uid,
            "authorName" to authorName,
            "authorAvatar" to authorAvatar,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            val docRef = commentsCol.add(payload).await()
            postDoc.update(
                "commentsCount", FieldValue.increment(1),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentsCol.path)
            throw e
        }
    }

    // --- Stories Operations ---
    fun observeStories(): Flow<List<Story>> {
        return db.collection("stories")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Story::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "stories")
                throw error
            }
    }

    suspend fun createStory(
        content: String,
        mediaColor: String,
        authorName: String,
        authorAvatar: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val storiesCol = db.collection("stories")
        val payload = mapOf(
            "userId" to uid,
            "authorName" to authorName,
            "authorAvatar" to authorAvatar,
            "content" to content,
            "mediaColor" to mediaColor,
            "createdAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            val docRef = storiesCol.add(payload).await()
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, storiesCol.path)
            throw e
        }
    }

    // --- Following Operations ---
    fun observeFollowing(userId: String): Flow<Set<String>> {
        val path = "users/$userId/following"
        return db.collection("users").document(userId).collection("following")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.id }.toSet()
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun toggleFollow(
        targetUserId: String,
        targetUsername: String,
        targetDisplayName: String,
        currentlyFollowing: Boolean
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val followDoc = db.collection("users").document(uid).collection("following").document(targetUserId)
        val userDoc = db.collection("users").document(uid)
        val targetUserDoc = db.collection("users").document(targetUserId)

        try {
            if (currentlyFollowing) {
                followDoc.delete().await()
                userDoc.update(
                    "followingCount", FieldValue.increment(-1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
                runCatching {
                    targetUserDoc.update(
                        "followersCount", FieldValue.increment(-1),
                        "updatedAt", FieldValue.serverTimestamp()
                    ).await()
                }
            } else {
                followDoc.set(mapOf(
                    "targetUserId" to targetUserId,
                    "userId" to uid,
                    "targetUsername" to targetUsername,
                    "targetDisplayName" to targetDisplayName,
                    "createdAt" to FieldValue.serverTimestamp()
                )).await()
                userDoc.update(
                    "followingCount", FieldValue.increment(1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
                runCatching {
                    targetUserDoc.update(
                        "followersCount", FieldValue.increment(1),
                        "updatedAt", FieldValue.serverTimestamp()
                    ).await()
                }
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, followDoc.path)
            throw e
        }
    }

    // --- Search Users ---
    fun searchUsers(query: String): Flow<List<UserProfile>> {
        val col = db.collection("users")
        val q = query.trim()
        val stream = if (q.isNotEmpty()) {
            col.orderBy("username")
                .startAt(q)
                .endAt(q + "\uf8ff")
                .limit(20)
        } else {
            col.orderBy("postsCount", Query.Direction.DESCENDING).limit(10)
        }

        return stream.snapshots()
            .map { snapshot ->
                snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, "users")
                throw error
            }
    }
}
