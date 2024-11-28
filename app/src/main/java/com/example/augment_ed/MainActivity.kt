package com.example.augment_ed

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.augment_ed.ui.theme.AugmentEDTheme
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException

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
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isArSupported) {
            MaterialIconButton(
                text = "Scan",
                icon = Icons.Filled.QrCodeScanner,
                onClick = { /* Handle Scan button click */ }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        MaterialIconButton(
            text = "Practice",
            icon = Icons.Filled.School,
            onClick = { /* Handle Practice button click */ }
        )
    }
}

@Composable
fun MaterialIconButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(120.dp)
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