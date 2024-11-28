package com.example.augment_ed

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.augment_ed.ui.theme.AugmentEDTheme
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import android.util.Log
import androidx.compose.foundation.layout.padding
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableException
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.fontResource

import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.fontResource

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.fontResource

val minecraftFont = FontFamily(
    Font(R.font.minecraftreg, FontWeight.Normal),
    Font(R.font.minecraftbold, FontWeight.Bold)
)

@Composable
fun MainScreen(modifier: Modifier = Modifier, isArSupported: Boolean, sensorX: Float, sensorY: Float) {
    val infiniteTransition = rememberInfiniteTransition()
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF0D1B2A), // Deep Space Blue
        targetValue = Color(0xFF1B263B), // Darker Blue
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1B263B), // Darker Blue
        targetValue = Color(0xFF415A77), // Aurora Blue
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF415A77), // Aurora Blue
        targetValue = Color(0xFF778DA9), // Light Aurora
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(color1, color2, color3)))
    ) {
        ParticleBackground(sensorX, sensorY)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { /* Handle background click */ },
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Augment-ED, welcome!",
                fontFamily = minecraftFont,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            if (isArSupported) {
                AnimatedMaterialIconButton(
                    text = "Scan",
                    icon = Icons.Filled.QrCodeScanner,
                    onClick = { /* Handle Scan button click */ }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedMaterialIconButton(
                text = "Practice",
                icon = Icons.Filled.School,
                onClick = { /* Handle Practice button click */ }
            )
        }
    }
}

@Composable
fun ParticleBackground(sensorX: Float, sensorY: Float) {
    val particles = remember { mutableStateListOf<Particle>() }
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    val screenWidth = with(LocalDensity.current) { screenWidthDp.toPx() }
    val screenHeight = with(LocalDensity.current) { screenHeightDp.toPx() }

    LaunchedEffect(Unit) {
        while (true) {
            particles.add(Particle(screenWidth, screenHeight))
            delay(50) // Reduce delay for smoother animation
        }
    }

    particles.forEach { particle ->
        particle.x += sensorX * 2
        particle.y += sensorY * 2
        if (particle.y > screenHeight) {
            particle.y = 0f
            particle.x = Random.nextFloat() * screenWidth
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = 0.3f), // Increase opacity for visibility
                radius = particle.size * 2, // Increase size for visibility
                center = Offset(particle.x, particle.y)
            )
        }
    }
}

data class Particle(
    var x: Float,
    var y: Float,
    val size: Float = Random.nextFloat() * 2f + 1f, // Increase base size
    val speed: Float = Random.nextFloat() * 2f + 1f,
    val color: Color = Color(
        Random.nextFloat(),
        Random.nextFloat(),
        Random.nextFloat(),
        0.3f // Increase base opacity
    )
) {
    constructor(screenWidth: Float, screenHeight: Float) : this(
        x = Random.nextFloat() * screenWidth,
        y = Random.nextFloat() * screenHeight
    )
}

@Composable
fun AnimatedMaterialIconButton(
    text: String = "Scan",
    icon: ImageVector = Icons.Filled.QrCodeScanner,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val size by animateDpAsState(if (isPressed) 140.dp else 120.dp, label = "")

    FilledTonalIconButton(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AugmentEDTheme {
        MainScreen(isArSupported = true, sensorX = 0f, sensorY = 0f)
    }
}