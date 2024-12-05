package com.google.ar.core.examples.kotlin.helloar

import android.net.Uri
import android.opengl.Matrix
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.Surface
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.sceneform.rendering.ModelRenderable

class HelloArRenderer(private val activity: HelloArActivity) : SampleRender.Renderer, DefaultLifecycleObserver {

  companion object {
    private const val TAG = "HelloArRenderer"
  }

  private var currentAnchor: Anchor? = null
  private var currentRenderable: ModelRenderable? = null // Store the loaded ModelRenderable
  private var keywordToModelMap: Map<String, String> = emptyMap()

  var currentFrame: Frame? = null
    private set

  // Set the mapping between keywords and model paths
  fun setKeywordToModelMap(map: Map<String, String>) {
    keywordToModelMap = map
  }

  // Configure the AR session
  fun configureSession(session: Session) {
    session.configure(
      session.config.apply {
        lightEstimationMode = com.google.ar.core.Config.LightEstimationMode.ENVIRONMENTAL_HDR
        planeFindingMode = com.google.ar.core.Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
      }
    )
  }

  override fun onSurfaceCreated(render: SampleRender) {
    Log.d(TAG, "Surface created for rendering.")
  }

  override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
    Log.d(TAG, "Surface changed: width=$width, height=$height")
  }

  override fun onDrawFrame(render: SampleRender) {
    val session = activity.arCoreSessionHelper.session ?: return

    try {
      val frame = session.update()
      currentFrame = frame
    } catch (e: Exception) {
      Log.e(TAG, "Error during frame update: ${e.message}")
      return
    }
  }

  // Render the model based on the detected keyword
  fun renderModelForKeyword(keyword: String) {
    val modelPath = keywordToModelMap[keyword] ?: return

    clearCurrentModel() // Clear the existing model

    // Load and render the model
    ModelRenderable.builder()
      .setSource(activity, Uri.parse(modelPath))
      .build()
      .thenAccept { renderable ->
        currentRenderable = renderable // Store the loaded renderable

        // Create an anchor at the camera's current pose
        val anchor = activity.arCoreSessionHelper.session?.createAnchor(
          currentFrame?.camera?.displayOrientedPose ?: return@thenAccept
        )
        currentAnchor = anchor

        // Create an AnchorNode and set the renderable
        val anchorNode = com.google.ar.sceneform.AnchorNode(anchor)
        anchorNode.renderable = renderable

        // Add the model to the scene
        activity.view.sceneView.scene.addChild(anchorNode)
      }
      .exceptionally { throwable ->
        Log.e(TAG, "Error loading 3D model: ${throwable.message}")
        null
      }
  }

  // Clear the current model rendering
  fun clearCurrentModel() {
    currentAnchor?.detach()
    currentAnchor = null
    currentRenderable = null
  }

  // Get the rotation degrees of the display
  fun getRotationDegrees(): Int {
    val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      activity.display
    } else {
      @Suppress("DEPRECATION")
      activity.windowManager.defaultDisplay
    }

    val rotation = display?.rotation ?: Surface.ROTATION_0

    return when (rotation) {
      Surface.ROTATION_0 -> 0
      Surface.ROTATION_90 -> 90
      Surface.ROTATION_180 -> 180
      Surface.ROTATION_270 -> 270
      else -> 0
    }
  }

  override fun onPause(owner: LifecycleOwner) {
    clearCurrentModel() // Clear any model when paused
  }
}