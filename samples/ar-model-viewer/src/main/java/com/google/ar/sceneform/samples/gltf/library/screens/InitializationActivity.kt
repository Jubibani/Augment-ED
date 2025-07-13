package com.google.ar.sceneform.samples.gltf.library.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream
import kotlin.system.measureTimeMillis

class InitializationActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var modelDir: File
    private val REQUEST_CODE_INSTALL_PERMISSION = 1234

    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("jna.nosys", "true")
        System.setProperty("jna.boot.library.path", applicationInfo.nativeLibraryDir)

        setContentView(R.layout.activity_initialization)
        progressBar = findViewById(R.id.progressBar)
        modelDir = File(cacheDir, "vosk-model-small-en-us-0.15")

        // 1. Check install permission BEFORE MainActivity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                showInstallPermissionDialog()
                return // Wait for permission result before continuing initialization!
            }
        }

        // Existing initialization code below
        Log.d("InitializationActivity", "Starting model file copy")
        CoroutineScope(Dispatchers.Main).launch {
            copyModelFiles()
            val modelPath = modelDir.absolutePath
            if (File(modelPath).exists() && File(modelPath).listFiles()?.isNotEmpty() == true) {
                Log.d("InitializationActivity", "Model files found at $modelPath")
                initializeModel(modelPath)
            } else {
                Log.e("InitializationActivity", "Model files not found at $modelPath")
            }
        }

        val voskSpeechRecognitionHelper = (application as App).voskSpeechRecognitionHelper

        voskSpeechRecognitionHelper.setProgressCallback { progress ->
            progressBar.progress = progress
        }

        if (!voskSpeechRecognitionHelper.isModelInitialized()) {
            progressBar.visibility = View.VISIBLE
            Log.d("InitializationActivity", "Waiting for model to be initialized")
        } else {
            Log.d("InitializationActivity", "Model already initialized")
            startMainActivity()
        }
    }

    private fun showInstallPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs permission to install mini-game APKs. Please allow this in the next screen.")
            .setCancelable(false)
            .setPositiveButton("Allow") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivityForResult(intent, REQUEST_CODE_INSTALL_PERMISSION)
            }
            .setNegativeButton("Exit") { _, _ ->
                Toast.makeText(this, "Permission is required to install games.", Toast.LENGTH_LONG).show()
                finish()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INSTALL_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (packageManager.canRequestPackageInstalls()) {
                    // Permission granted, continue initialization
                    recreate() // or call your initialization code here directly
                } else {
                    Toast.makeText(this, "Permission not granted. Cannot continue.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    @OptIn(UnstableApi::class) private suspend fun initializeModel(modelPath: String) {
        Log.d("InitializationActivity", "Starting model initialization")
        withContext(Dispatchers.Main) {
            try {
                val voskSpeechRecognitionHelper = (application as App).voskSpeechRecognitionHelper
                val loadingTime = measureTimeMillis {
                    withContext(Dispatchers.IO) {
                        Log.d("InitializationActivity", "Initializing model at $modelPath")
                        val model = Model(modelPath)
                        voskSpeechRecognitionHelper.initializeModel(model)
                        Log.d("InitializationActivity", "Model initialized successfully")
                    }
                }
                Log.d("InitializationActivity", "Model loading time: ${loadingTime / 1000} seconds")
                voskSpeechRecognitionHelper.setModelInitializedCallback {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@InitializationActivity, "Model loaded successfully", Toast.LENGTH_SHORT).show()
                    Log.d("InitializationActivity", "Model loaded successfully")
                    startMainActivity()
                }
            } catch (e: Exception) {
                Log.e("InitializationActivity", "Error initializing model: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    @OptIn(UnstableApi::class) private suspend fun copyModelFiles() = withContext(Dispatchers.IO) {
        val assetManager = assets
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        try {
            val zipFileName = "model/vosk-model-small-en-us-0.15.zip"
            val zipFile = assetManager.open(zipFileName)
            Log.d("InitializationActivity", "Found zip file: $zipFileName")
            unzip(zipFile, modelDir)
            Log.d("InitializationActivity", "Unzipped model files to ${modelDir.absolutePath}")
            modelDir.listFiles()?.forEach { file ->
                Log.d("InitializationActivity", "Unzipped file: ${file.absolutePath}")
            }
        } catch (e: IOException) {
            Log.e("InitializationActivity", "Error unpacking model files: ${e.message}")
            e.printStackTrace()
        }
    }

    @OptIn(UnstableApi::class) private fun unzip(zipInputStream: InputStream, destDir: File) {
        ZipInputStream(zipInputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                // Skip the first directory level if present
                val name = entry.name.split("/").drop(1).joinToString("/")
                if (name.isNotEmpty()) {
                    val file = File(destDir, name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { fos ->
                            val buffer = ByteArray(1024)
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                fos.write(buffer, 0, len)
                            }
                        }
                    }
                }
                entry = zis.nextEntry
            }
            zis.closeEntry()
        }
        destDir.listFiles()?.forEach { file ->
            Log.d("InitializationActivity", "Unzipped file: ${file.absolutePath}")
        }
    }

    @OptIn(UnstableApi::class) private fun startMainActivity() {
        Log.d("InitializationActivity", "Starting MainActivity")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}