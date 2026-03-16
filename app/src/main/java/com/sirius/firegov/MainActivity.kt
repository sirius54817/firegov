package com.sirius.firegov

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sirius.firegov.ui.navigation.NavGraph
import com.sirius.firegov.ui.theme.FiregovTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var logoutJob: Job? = null
    private val resetTimerEvent = mutableStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FiregovTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val timerTrigger by resetTimerEvent

                // Reset timer on any user interaction detected in the Activity
                LaunchedEffect(timerTrigger) {
                    logoutJob?.cancel()
                    logoutJob = scope.launch {
                        delay(10 * 60 * 1000) // 10 minutes
                        val auth = FirebaseAuth.getInstance()
                        if (auth.currentUser != null) {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController = navController)
                }
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimerEvent.value = System.currentTimeMillis()
    }
}
