package com.google.ar.core.examples.kotlin.ui.theme.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.augment_ed.ui.theme.AugmentEDTheme
import com.google.ar.core.examples.kotlin.libraryscan.LibraryScanActivity
import kotlin.math.log

class LibraryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AugmentEDTheme {
                LibraryScreen()
            }
        }
    }
}

@Composable
fun LibraryScreen() {
    // List of 3D models mapped to their file paths
    val modelMap = listOf(
        "Bacteria" to "models/Bacteria.obj",
        "Heart" to "librarymodel/realistic_human_heart.glb"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(modelMap) { (modelName, modelPath) ->
            ModelItem(modelName = modelName, modelPath = modelPath)
        }
    }
}

@Composable
fun ModelItem(modelName: String, modelPath: String) {
    val context = LocalContext.current

    Button(
        onClick = {
            // Navigate to LibraryScanActivity with the selected model's path
            val intent = Intent(context, LibraryScanActivity::class.java).apply {
                putExtra("modelPath", modelPath) // Pass model path as an Intent extra
                Log.d("LibraryScanActivity", "Received model path: $modelPath")
            }
            context.startActivity(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = modelName)
    }
}