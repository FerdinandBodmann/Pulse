package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

private const val TAG = "PulseApplication"

class PulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized()
    }

    private fun ensureFirebaseInitialized() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val appId = getString(R.string.google_app_id)
                val projectId = getString(R.string.project_id)
                val apiKey = getString(R.string.google_api_key)
                val gcmSenderId = getString(R.string.gcm_defaultSenderId)

                val options = FirebaseOptions.Builder()
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .setApiKey(apiKey)
                    .setGcmSenderId(gcmSenderId)
                    .build()

                FirebaseApp.initializeApp(this, options)
                Log.i(TAG, "FirebaseApp successfully initialized programmatically.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseApp", e)
        }
    }
}
