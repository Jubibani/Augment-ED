package com.example.augment_ed.ui.theme.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.augment_ed.MainActivity
import com.example.augment_ed.R
import kotlinx.coroutines.delay
import androidx.compose.runtime.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                visible = true
                delay(4000) // Duration of the splash screen
                visible = false
                delay(500) // Duration of the fade-out animation
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White), // Set the background color to white
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(1000)), // Fade in over 1 second
                    exit = fadeOut(animationSpec = tween(500)) // Fade out over 0.5 seconds
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.splashscreentext),
                        contentDescription = "Splash Screen"
                    )
                }
            }
        }
    }
}