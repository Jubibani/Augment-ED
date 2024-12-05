package com.google.ar.core.examples.kotlin.helloar

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.examples.kotlin.common.helpers.ARCoreSessionLifecycleHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class HelloArActivity : AppCompatActivity() {

  companion object {
    private const val TAG = "HelloArActivity"
  }

  lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
  lateinit var view: HelloArView
  private lateinit var renderer: HelloArRenderer

  private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
  private var currentKeyword: String? = null

  private val keywordToModelMap = mapOf(
    "amphibian" to "assets/librarymodel/model_6_-_marine_toad_on_leaf.glb",
    "platypus" to "assets/librarymodel/platypus.glb",
    "bacteria" to "assets/librarymodel/archaea.glb",
    "digestive" to "assets/librarymodel/digestive-system.glb"
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
    lifecycle.addObserver(arCoreSessionHelper)

    renderer = HelloArRenderer(this)
    renderer.setKeywordToModelMap(keywordToModelMap)

    view = HelloArView(this)
    lifecycle.addObserver(view)
    setContentView(view.root)

    arCoreSessionHelper.beforeSessionResume = renderer::configureSession
    lifecycle.addObserver(renderer)

    startTextRecognition()
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun startTextRecognition() {
    view.sceneView.setOnTouchListener { _, _ ->
      val frame = renderer.currentFrame ?: return@setOnTouchListener false
      try {
        val image = frame.acquireCameraImage()
        val rotationDegrees = renderer.getRotationDegrees()
        val inputImage = InputImage.fromMediaImage(image, rotationDegrees)

        textRecognizer.process(inputImage)
          .addOnSuccessListener { visionText ->
            val detectedText = visionText.text.lowercase()
            handleRecognizedText(detectedText)
          }
          .addOnFailureListener { e ->
            Log.e(TAG, "Text recognition failed: ${e.message}")
          }
          .addOnCompleteListener {
            image.close()
          }
        true
      } catch (e: Exception) {
        Log.w(TAG, "Camera image not available yet.")
        false
      }
    }
  }

  private fun handleRecognizedText(text: String) {
    val keyword = keywordToModelMap.keys.find { text.contains(it) }
    if (keyword == currentKeyword) return

    if (keyword != null) {
      currentKeyword = keyword
      renderer.renderModelForKeyword(keyword)
    } else {
      renderer.clearCurrentModel()
      currentKeyword = null
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    textRecognizer.close()
  }
}