package com.example.augment_ed

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private var mUserRequestedInstall = true
    private var mSession: Session? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue with AR session creation.
            tryCreateArSession()
        } else {
            // Permission is denied. Show a message to the user.
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AugmentEDTheme {
                MainScreen(isArSupported = mSession != null)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // ARCore requires camera permission to operate.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            return
        }

        // Try to create AR session if permission is already granted.
        tryCreateArSession()
    }

    private fun tryCreateArSession() {
        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        // Success: Safe to create the AR session.
                        mSession = Session(this)
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        // Installation requested, return to avoid session creation.
                        mUserRequestedInstall = false
                        return
                    }
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(this, "ARCore installation was declined by the user.", Toast.LENGTH_LONG).show()
            return
        } catch (e: Exception) {
            // Handle other exceptions.
            Toast.makeText(this, "ARCore is not available: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, isArSupported: Boolean) {
    Box(modifier = modifier.fillMaxSize()) {
        // Dynamic background with particles
        ParticleBackground()

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
fun ParticleBackground() {
    val particles = remember { mutableStateListOf<Particle>() }
    val infiniteTransition = rememberInfiniteTransition()

    // Add new particles periodically
    LaunchedEffect(Unit) {
        while (true) {
            particles.add(Particle())
            delay(100) // Add a new particle every 100ms
        }
    }

    // Update particle positions
    particles.forEach { particle ->
        particle.y += particle.speed
        if (particle.y > 1000f) { // Reset particle if it goes off screen
            particle.y = 0f
            particle.x = Random.nextFloat() * 1000f
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
    }
}

data class Particle(
    var x: Float = Random.nextFloat() * 1000f,
    var y: Float = Random.nextFloat() * 1000f,
    val size: Float = Random.nextFloat() * 5f + 2f,
    val speed: Float = Random.nextFloat() * 2f + 1f,
    val color: Color = Color(
        Random.nextFloat(),
        Random.nextFloat(),
        Random.nextFloat(),
        0.5f
    )
)

@Composable
fun AnimatedMaterialIconButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val size by animateDpAsState(if (isPressed) 140.dp else 120.dp)

    FilledTonalIconButton(
        onClick = onClick,
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
        MainScreen(isArSupported = true)
    }
}