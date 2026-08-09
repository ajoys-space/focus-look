package com.focuslock.app.ui.screens.blocking

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.focuslock.app.ui.theme.FocusLockTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Shown instead of the locked app. Uses showWhenLocked/turnScreenOn (both
 * safe from API 27+, and our minSdk is 29) so it appears reliably even if
 * the device screen was off.
 *
 * HONEST LIMITATION: the Back button cannot be disabled — Android reserves
 * that control for the user. We redirect Back to the home screen instead
 * of letting it dismiss into the blocked app underneath.
 */
@AndroidEntryPoint
class BlockingActivity : ComponentActivity() {

    companion object {
        private const val TAG = "BlockingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ${intent.getStringExtra("locked_package_name")}")

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        setContent {
            FocusLockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BlockingScreen(onFinish = { finish() })
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ${intent.getStringExtra("locked_package_name")}")
        setIntent(intent)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed")
        // Instead of just moveTaskToBack, explicitly navigate to Home.
        // This is more robust against the Activity being re-launched 
        // immediately by the Accessibility Service.
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }
}