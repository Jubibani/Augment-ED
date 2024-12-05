package com.google.ar.core.examples.kotlin.helloar

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import com.google.ar.core.examples.kotlin.common.helpers.ARCoreSessionLifecycleHelper
import android.view.Surface

class HelloArActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HelloArActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var anchorNode: AnchorNode
    private lateinit var arCoreSessionLifecycleHelper: ARCoreSessionLifecycleHelper
    private lateinit var arRenderer: HelloArRenderer
    private lateinit var arFragment: ArFragment
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var currentKeyword: String? = null

    // Map of keywords to 3D models
    private val keywordToModelMap = mapOf(
        "amphibian" to "models/amphibian.glb",
        "platypus" to "models/platypus.glb",
        "bacteria" to "models/bacteria.glb",
        "digestive" to "models/digestive.glb"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasCameraPermission()) {
            requestCameraPermission()
        }

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            scanTextAtHit(hitResult)
        }
        // Initialize ARCore lifecycle helper
        arCoreSessionLifecycleHelper = ARCoreSessionLifecycleHelper(this)
        // Initialize renderer
        arRenderer = HelloArRenderer(this, arCoreSessionLifecycleHelper)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun scanTextAtHit(hitResult: HitResult) {
        val frame = arFragment.arSceneView.arFrame ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val image = frame.acquireCameraImage()
                val rotationDegrees = getRotationDegrees()
                val inputImage = InputImage.fromMediaImage(image, rotationDegrees)

                val visionText = textRecognizer.process(inputImage).await()
                val detectedText = visionText.text.lowercase()

                withContext(Dispatchers.Main) {
                    handleRecognizedText(detectedText, hitResult)
                }

                image.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error in text recognition: ${e.message}")
            }
        }
    }

    private fun getRotationDegrees(): Int {
        val rotation = windowManager.defaultDisplay.rotation
        return when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    private fun handleRecognizedText(text: String, hitResult: HitResult) {
        val keyword = keywordToModelMap.keys.find { text.contains(it) }
        if (keyword == currentKeyword) return

        if (keyword != null) {
            currentKeyword = keyword
            renderModelForKeyword(keyword, hitResult)
            Toast.makeText(this, "Detected: $keyword", Toast.LENGTH_SHORT).show()
        } else {
            // Clear previous model if no keyword is found
            currentKeyword = null
        }
    }

    private fun renderModelForKeyword(keyword: String, hitResult: HitResult) {
        val modelPath = keywordToModelMap[keyword] ?: return

        ModelRenderable.builder()
            .setSource(this, android.net.Uri.parse(modelPath))
            .build()
            .thenAccept { renderable ->
                val anchorNode = AnchorNode(hitResult.createAnchor())
                anchorNode.renderable = renderable
                arFragment.arSceneView.scene.addChild(anchorNode)
                Log.d(TAG, "Model rendered for keyword: $keyword")
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Unable to load model for $keyword: $throwable")
                Toast.makeText(this, "Unable to load model for $keyword", Toast.LENGTH_SHORT).show()
                null
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        textRecognizer.close()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!hasCameraPermission()) {
            Toast.makeText(this, "Camera permission is required for AR functionality.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    fun placeModel(keyword: String, anchor: Anchor) {
        val modelPath = keywordToModelMap[keyword] ?: return
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelPath))
            .build()
            .thenAccept { renderable ->
                // Create and attach AnchorNode
                val anchorNode = AnchorNode(anchor)
                anchorNode.setRenderable(renderable)
                arFragment.arSceneView.scene.addChild(anchorNode)
            }
            .exceptionally { throwable ->
                Log.e("HelloArActivity", "Error loading model: $modelPath", throwable)
                null
            }
    }

}