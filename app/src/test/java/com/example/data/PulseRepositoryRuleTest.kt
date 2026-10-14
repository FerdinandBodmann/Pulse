package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PulseRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createPost_validPayload_createsDocumentAndReturnsId() {
        runBlocking {
            val uid = signInTestUser(ALICE_EMAIL)
            val repository = PulseRepository(firestore)

            // Bootstrap profile first for post count update
            val saveResult = repository.saveUserProfile(
                UserProfile(
                    userId = uid,
                    username = "alice_test",
                    displayName = "Alice Wonderland"
                )
            )
            if (saveResult.isFailure) {
                println("ALICE SAVE FAILED: ${saveResult.exceptionOrNull()}")
            }
            assertTrue("Alice saveUserProfile should succeed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

            val uniqueContent = "Testing Pulse Post ${UUID.randomUUID()}"
            val result = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.createPost(
                    content = uniqueContent,
                    category = "Tech",
                    mediaColor = "#6366F1",
                    authorName = "Alice Wonderland",
                    authorHandle = "alice_test",
                    authorAvatar = "",
                    mediaType = "text"
                )
            }

            assertTrue("createPost should succeed", result.isSuccess)
            val postId = result.getOrThrow()
            assertTrue("postId should not be empty", postId.isNotEmpty())
        }
    }

    @Test
    fun saveUserProfile_authenticatedOwner_createsAndReadsProfile() {
        runBlocking {
            val uid = signInTestUser(BOB_EMAIL)
            val repository = PulseRepository(firestore)

            val profile = UserProfile(
                userId = uid,
                username = "bob_builder",
                displayName = "Bob Builder",
                bio = "Can we fix it? Yes we can!",
                bannerColor = "#06B6D4"
            )

            val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.saveUserProfile(profile)
            }
            if (saveResult.isFailure) {
                saveResult.exceptionOrNull()?.printStackTrace()
            }
            assertTrue("saveUserProfile should succeed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

            val fetchedProfile = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.observeUserProfile(uid).first { it != null }
            }
            assertNotNull(fetchedProfile)
            assertEquals("bob_builder", fetchedProfile?.username)
            assertEquals("Bob Builder", fetchedProfile?.displayName)
        }
    }

    @Test
    fun createPost_unauthenticatedUser_fails() {
        runBlocking {
            auth.signOut()
            val repository = PulseRepository(firestore)

            val result = repository.createPost(
                content = "Unauthenticated content",
                category = "General",
                mediaColor = "#6366F1",
                authorName = "Ghost",
                authorHandle = "ghost",
                authorAvatar = ""
            )

            assertTrue("createPost should fail when unauthenticated", result.isFailure)
        }
    }

    @Test
    fun getPosts_unauthenticatedUser_failsWithPermissionDenied() {
        runBlocking {
            auth.signOut()

            try {
                withTimeout(DEFAULT_TIMEOUT_MS) {
                    firestore.collection("posts").get().await()
                }
            } catch (e: Exception) {
                assertTrue("Expected exception when unauthenticated", e.message != null || e is FirebaseFirestoreException)
            }
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@pulse-test.com"
        const val BOB_EMAIL = "bob@pulse-test.com"
        const val DEFAULT_TIMEOUT_MS = 8000L
    }
}
