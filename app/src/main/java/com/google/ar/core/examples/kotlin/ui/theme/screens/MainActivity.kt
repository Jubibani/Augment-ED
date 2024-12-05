package com.google.ar.core.examples.kotlin.ui.theme.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.augment_ed.ui.theme.AugmentEDTheme
import com.example.textrecognition.RefinedTextRecognitionScreen
import com.google.ar.core.examples.kotlin.helloar.HelloArActivity
import com.google.ar.core.examples.kotlin.helloar.R
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AugmentEDTheme {
                MainScreen()
            }
        }
    }
}

val MinecraftFontFamily = FontFamily(
    Font(R.font.minecraftregular, FontWeight.Normal),
    Font(R.font.minecraftbold, FontWeight.Bold)
)

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var showTextRecognition by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF0D1B2A),
        targetValue = Color(0xFF1B263B),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1B263B),
        targetValue = Color(0xFF415A77),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF415A77),
        targetValue = Color(0xFF778DA9),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(color1, color2, color3)))
    ) {
        ParticleBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedText("Augment-ED", 45, FontWeight.ExtraBold, MinecraftFontFamily, Color(0xFFD4AF37), Color(0xFFE5C158))
            AnimatedText("Welcome!", 24, FontWeight.Normal, MinecraftFontFamily, Color.White, Color(0xFF778DA9))

            Spacer(modifier = Modifier.height(25.dp))

            if (showTextRecognition) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Replace this with Text Recognition composable
                    Text(text = "Text Recognition Screen", color = Color.White)
                }

                Spacer(modifier = Modifier.height(22.dp))

                AnimatedMaterialIconButton(
                    text = "Back to Main Menu",
                    icon = Icons.Filled.ArrowBack,
                    onClick = { showTextRecognition = false }
                )
            } else {
                AnimatedMaterialIconButton(
                    text = "Scan",
                    icon = Icons.Filled.QrCodeScanner,
                    onClick = {
                        val intent = Intent(context, HelloArActivity::class.java)
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(22.dp))

                AnimatedMaterialIconButton(
                    text = "Practice",
                    icon = Icons.Filled.School,
                    onClick = { /* TODO */ }
                )
                Spacer(modifier = Modifier.height(22.dp))

                AnimatedMaterialIconButton(
                    text = "Library",
                    icon = Icons.Filled.LibraryBooks,
                    onClick = { /* TODO */ }
                )
                Spacer(modifier = Modifier.height(22.dp))

                AnimatedMaterialIconButton(
                    text = "Text Recognition",
                    icon = Icons.Filled.TextFields,
                    onClick = {

                        showTextRecognition = true

                    }
                )
            }
        }
    }
}

@Composable
fun ParticleBackground() {
    val maxParticles = 500 // Limit the number of particles
    val particles = remember { mutableStateListOf<Particle>() }
    val random = remember { Random.Default }

    // Add new particles periodically, but respect the maximum
    LaunchedEffect(Unit) {
        while (true) {
            if (particles.size < maxParticles) {
                particles.add(Particle(random))
            }
            delay(50L) // Adjust for particle generation frequency
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            // Update particle properties
            particle.update()

            // Remove particles if they complete their lifecycle
            if (particle.alpha <= 0f || particle.size <= 0f) {
                iterator.remove()
            } else {
                // Draw the particle
                drawCircle(
                    color = Color.White.copy(alpha = particle.alpha),
                    radius = particle.size,
                    center = particle.position
                )
            }
        }
    }
}

// Particle class to manage properties
data class Particle(
    val random: Random,
    var position: Offset = Offset(random.nextFloat() * 1080, random.nextFloat() * 1920),
    var alpha: Float = random.nextFloat(),
    var size: Float = random.nextFloat() * 3f + 1f,
    var velocity: Offset = Offset(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f)
) {
    fun update() {
        position += velocity
        alpha -= 0.01f // Fade out gradually
        size = maxOf(size - 0.05f, 0f) // Shrink size
    }
}


@Composable
fun AnimatedMaterialIconButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val containerColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFD4AF37),
        targetValue = Color(0xFFE5C158),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnimatedText(
    text: String,
    fontSize: Int,
    fontWeight: FontWeight,
    fontFamily: FontFamily,
    startColor: Color,
    endColor: Color,
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Animate text color
    val animatedColor by infiniteTransition.animateColor(
        initialValue = startColor,
        targetValue = endColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animate scale for pulsing effect
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = text,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        color = animatedColor,
        modifier = Modifier.scale(scale)
    )
}