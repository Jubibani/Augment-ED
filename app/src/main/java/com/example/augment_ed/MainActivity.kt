package com.example.augment_ed

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
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush

class MainActivity : ComponentActivity(), SensorEventListener {

    private var mUserRequestedInstall = true
    private var mSession: Session? = null
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var sensorX = 0f
    private var sensorY = 0f

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            tryCreateArSession()
        } else {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            AugmentEDTheme {
                MainScreen(isArSupported = mSession != null, sensorX = sensorX, sensorY = sensorY)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            return
        }

        tryCreateArSession()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun tryCreateArSession() {
        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        mSession = Session(this)
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        mUserRequestedInstall = false
                        return
                    }
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(this, "ARCore installation was declined by the user.", Toast.LENGTH_LONG).show()
            return
        } catch (e: Exception) {
            Toast.makeText(this, "ARCore is not available: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0]
            sensorY = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}

@Composable
fun MainScreen(modifier: Modifier = Modifier, isArSupported: Boolean, sensorX: Float, sensorY: Float) {
    val infiniteTransition = rememberInfiniteTransition()
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF3F51B5),
        targetValue = Color(0xFF2196F3),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF2196F3),
        targetValue = Color(0xFF3F51B5),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(color1, color2)))
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
            delay(100)
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
                color = particle.color,
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
    }
}

data class Particle(
    var x: Float,
    var y: Float,
    val size: Float = Random.nextFloat() * 5f + 2f,
    val speed: Float = Random.nextFloat() * 2f + 1f,
    val color: Color = Color(
        Random.nextFloat(),
        Random.nextFloat(),
        Random.nextFloat(),
        0.5f
    )
) {
    constructor(screenWidth: Float, screenHeight: Float) : this(
        x = Random.nextFloat() * screenWidth,
        y = Random.nextFloat() * screenHeight
    )
}

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
        MainScreen(isArSupported = true, sensorX = 0f, sensorY = 0f)
    }
}