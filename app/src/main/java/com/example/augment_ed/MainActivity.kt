package com.example.augment_ed

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
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
import com.google.ar.core.exceptions.UnavailableException
import kotlin.random.Random
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.augment_ed.data.AppDatabase
import com.example.augment_ed.data.Concept
import com.example.augment_ed.data.ConceptDao
import com.example.augment_ed.data.ConceptRepository
import com.example.augment_ed.data.DatabaseInitializer
//import com.example.augment_ed.viewmodels.ARViewModel
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.Manifest
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


private fun isARCoreSupportedAndUpToDate(context: Context): Boolean {
    val availability = ArCoreApk.getInstance().checkAvailability(context)
    return when (availability) {
        ArCoreApk.Availability.SUPPORTED_INSTALLED -> true

        ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
        ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
            try {
                // Request ARCore installation or update if needed.
                val installStatus = ArCoreApk.getInstance().requestInstall(context as Activity, true)
                when (installStatus) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        Log.i("MainActivity", "ARCore installation requested.")
                        false
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> true
                }
            } catch (e: UnavailableException) {
                Log.e("MainActivity", "ARCore not installed", e)
                false
            }
        }

        ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
            // This device is not supported for AR.
            false
        }

        ArCoreApk.Availability.UNKNOWN_CHECKING,
        ArCoreApk.Availability.UNKNOWN_ERROR,
        ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> {
            // Handle the error appropriately.
            false
        }
    }
}


class ARViewModelFactory(private val repository: ConceptRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ARViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ARViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ARViewModel(private val repository: ConceptRepository) : ViewModel() {
    fun startARScan() {
        // Implement AR scanning logic here
        // This could involve starting an AR session, processing camera frames, etc.
    }

    // ... other ViewModel functions
}

@Database(entities = [Concept::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conceptDao(): ConceptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
class MainActivity : ComponentActivity(), SensorEventListener {

    private var mUserRequestedInstall = true
    private var mSession: Session? = null
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var sensorX = 0f
    private var sensorY = 0f
    
    //AR
    private lateinit var arCoreSession: Session
    private var userRequestedInstall = false

    private lateinit var conceptRepository: ConceptRepository
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupArSession()
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
        
        
        //sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        val database = AppDatabase.getDatabase(applicationContext)
        conceptRepository = ConceptRepository(database.conceptDao())

        // Initialize database (if needed)
        lifecycleScope.launch {
            DatabaseInitializer(applicationContext).initializeDatabase()
        }
        
        setContent {
            AugmentEDTheme {
                var isVisible by remember { mutableStateOf(false) }

                // Start the animation after a delay
                LaunchedEffect(Unit) {
                    isVisible = true
                }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(3000)),
                    exit = fadeOut(animationSpec = tween(1000))
                ) {
                    MainScreen(isArSupported = mSession != null, sensorX = sensorX, sensorY = sensorY)
                }

            }
        }
    }

     fun setupArSession() {
         // Check AR availability
         when (ArCoreApk.getInstance().checkAvailability(this)) {
             ArCoreApk.Availability.SUPPORTED_INSTALLED -> setupArSession()
             ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD, ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                 try {
                     when (ArCoreApk.getInstance().requestInstall(this, userRequestedInstall)) {
                         ArCoreApk.InstallStatus.INSTALLED -> setupArSession()
                         ArCoreApk.InstallStatus.INSTALL_REQUESTED -> userRequestedInstall = true
                     }
                 } catch (e: UnavailableException) {
                     Toast.makeText(this, "ARCore not available", Toast.LENGTH_LONG).show()
                 }
             }
             ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE ->
                 Toast.makeText(this, "Your device does not support AR", Toast.LENGTH_LONG).show()
             ArCoreApk.Availability.UNKNOWN_CHECKING, ArCoreApk.Availability.UNKNOWN_ERROR, ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> {
                 // Handle other cases
             }
         }
         
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                arCoreSession = Session(this)
            } catch (e: UnavailableException) {
                Toast.makeText(this, "Unable to create AR session", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupArSession()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
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

// Define the Minecraft font family
val MinecraftFontFamily = FontFamily(
    Font(R.font.minecraftregular, FontWeight.Normal),
    Font(R.font.minecraftbold, FontWeight.Bold)

)



@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isArSupported: Boolean,
    sensorX: Float,
    sensorY: Float
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = ConceptRepository(database.conceptDao())
    val viewModel: ARViewModel = viewModel(factory = ARViewModelFactory(repository))

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

        val textOffsetY by animateDpAsState(
            targetValue = 0.dp,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )

        val idleAnimation = rememberInfiniteTransition()
        val idleOffsetY by idleAnimation.animateFloat(
            initialValue = -5f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Transparent,
            onClick = {
                // Handle background click
                // For example, you could show a toast message:
                // Toast.makeText(LocalContext.current, "Background clicked", Toast.LENGTH_SHORT).show()
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(color1, color2, color3)))
            ) {
                ParticleBackground(sensorX, sensorY)

                val textOffsetY by animateDpAsState(
                    targetValue = 0.dp,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )

                val idleAnimation = rememberInfiniteTransition()
                val idleOffsetY by idleAnimation.animateFloat(
                    initialValue = -5f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(0, (textOffsetY + idleOffsetY.dp).roundToPx()) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Augment-ED",
                        fontSize = 45.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD4AF37),
                        fontFamily = MinecraftFontFamily,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "welcome!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        modifier = Modifier.padding(bottom = 50.dp)
                    )

                    if (isArSupported) {
                        AnimatedMaterialIconButton(
                            text = "Scan",
                            icon = Icons.Filled.QrCodeScanner,
                            onClick = {
                                // Trigger AR scan
                                viewModel.startARScan()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    AnimatedMaterialIconButton(
                        text = "Practice",
                        icon = Icons.Filled.School,
                        onClick = { /* Handle Practice button click */ }
                    )
                }
            }
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
    val scale by animateFloatAsState(if (isPressed) 1.1f else 1.0f, label = "")

    FilledTonalIconButton(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .size(size)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = Color(0xFFD4AF37)
        )
    ) {
        // Button content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(48.dp),
                tint = Color.White, // White color for the icon
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontFamily = MinecraftFontFamily, // Use the Minecraft font
                color = Color.White, // White color for the text
            )

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


