package com.google.ar.sceneform.samples.gltf.library

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.media.Image
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView

import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity
import com.google.ar.sceneform.samples.gltf.library.helpers.MediaImageToBitmapHelper
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.gorisse.thomas.sceneform.scene.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.google.ar.sceneform.samples.gltf.library.HighlightOverlayView
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.withContext

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var arFragment: ArFragment
    private val arSceneView get() = arFragment.arSceneView
    private val scene get() = arSceneView.scene

    //for scan to text recognition then render
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var isModelPlaced = false
    private lateinit var instructionLabel: TextView


    //for scan to text recognition then display
    private lateinit var showTextRecognizer: TextRecognizer
    private lateinit var highlightOverlayView: HighlightOverlayView



    private var lastToastTime = 0L
    private val TOAST_COOLDOWN_MS = 20000 // 20 seconds cooldown

    private lateinit var modelDao: ModelDao
    private val recognizableModelNames = mutableListOf<String>()

    private val models = mutableMapOf<String, Renderable?>()
    private val modelViews = mutableMapOf<String, ViewRenderable?>()
    private val modelInfoMap = mutableMapOf<String, ModelEntity>()  // Store fetched models


    // Instead of lateinit, initialize LiveData properly
    private val modelLiveData: LiveData<List<ModelEntity>> by lazy {
        AppDatabase.getDatabase(requireContext(), lifecycleScope).modelDao().getAllModels()
    }

    //sounds
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var ping: MediaPlayer
    private lateinit var on: MediaPlayer
    private lateinit var off: MediaPlayer
    private lateinit var riser: MediaPlayer
    private lateinit var powerdown: MediaPlayer
    private lateinit var equip: MediaPlayer
    private lateinit var unequip: MediaPlayer

    private var pingJob: Job? = null
    private var lastPingTime = 0L
    private val PING_DEBOUNCE_TIME = 2000L // 2 seconds

    //hiding
    private lateinit var infoButton: FloatingActionButton
    private var isInfoVisible = false

    //Other Buttons
    private lateinit var libraryBackButton: FloatingActionButton
    private lateinit var switchButton: SwitchMaterial


    //vide description
    private lateinit var fullscreenToggle: ImageButton
    private lateinit var watchButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var playerView: PlayerView
    private lateinit var videoOverlayContainer: FrameLayout
    private var exoPlayer: ExoPlayer? = null


    //restart
    private lateinit var restartButton: FloatingActionButton

    // Declare magnifyingGlassNode and modelRenderable
    private var magnifyingGlassNode: Node? = null
    private var modelRenderable: ModelRenderable? = null
    private lateinit var magnifyingGlassButton: ImageButton
    // Display the cropped bitmap in the ImageView for debugging
    private lateinit var croppedImageView: ImageView

    //flags
    private var isTextRecognitionActive = false
    private var isRecognitionCancelled = false
    private var isRiserCancelled = false
    private var hasPlaneBeenDetectedOnce = false
    private var isFullscreen = false


    @OptIn(ExperimentalGetImage::class) @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext(), CoroutineScope(Dispatchers.IO))
        instructionLabel = view.findViewById<TextView>(R.id.instructionLabel)
        modelDao = database.modelDao()

        arFragment = childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment


        modelLiveData.observe(viewLifecycleOwner) { modelsList ->
            if (modelsList.isEmpty()) {
                Log.e("MainFragment", "Database is empty! Models were not inserted.")
            } else {
                Log.d("MainFragment", "Loaded models: ${modelsList.map { it.name }}")
            }
            modelInfoMap.clear()
            modelsList.forEach { modelInfoMap[it.name] = it }

            recognizableModelNames.clear()
            recognizableModelNames.addAll(modelsList.map { it.name })
        }

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(context, R.raw.popup)
        ping = MediaPlayer.create(context, R.raw.sonarping)
        on = MediaPlayer.create(context, R.raw.on)
        off = MediaPlayer.create(context, R.raw.off)
        riser = MediaPlayer.create(context, R.raw.riser)
        powerdown = MediaPlayer.create(context, R.raw.powerdown)
        equip = MediaPlayer.create(context, R.raw.equip)
        unequip = MediaPlayer.create(context, R.raw.unequip)

        // Initialize views
        infoButton = view.findViewById(R.id.infoButton)
        magnifyingGlassButton = view.findViewById(R.id.magnifyingGlassButton)
        restartButton = view.findViewById(R.id.refreshButton)

        // Initialize HighlightOverlayView
        highlightOverlayView = HighlightOverlayView(requireContext())

        //Initialize VideoDescription
        fullscreenToggle = requireView().findViewById(R.id.fullscreenToggle)
        playerView = view.findViewById(R.id.playerView)
        videoOverlayContainer = view.findViewById(R.id.videoOverlayContainer)
        watchButton = view.findViewById(R.id.watchButton)
        closeButton = view.findViewById(R.id.closeButton)
        // Initially hide the video UI
        videoOverlayContainer.visibility = View.GONE

        // Add HighlightOverlayView to the root FrameLayout
        val rootLayout = view as FrameLayout
        rootLayout.addView(highlightOverlayView)

        // Set buttons to be hidden by default
        magnifyingGlassButton.visibility = View.GONE
        restartButton.visibility = View.GONE

        //Initialize other buttons
        //!these are from another layout, especially from an Activity, so we use [requireActivity]
        libraryBackButton = requireActivity().findViewById(R.id.libraryBackButton)

        switchButton = requireActivity().findViewById(R.id.switchButton)

        infoButton.setOnClickListener {
            toggleInfoVisibility()
        }


        arFragment = (childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
            setOnSessionConfigurationListener { session, config ->
                // Modify the AR session configuration here
            }
            setOnViewCreatedListener { arSceneView ->
                arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
                setupTextRecognition(arSceneView)

                checkDetectedPLaneForEntry()
                setupRecognizableModelNames()

                /*           highlightRecognizedWords(arSceneView, highlightOverlayView, textRecognizer)*/
            }
        }

        // Magnifying glass button
        magnifyingGlassButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d("MainFragment", "Button pressed: showing magnifying glass")
                    showMagnifyingGlass()
                    startTextRecognition()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.d("MainFragment", "Button released: hiding magnifying glass")
                    hideMagnifyingGlass()
                    stopTextRecognition()
                    true
                }
                else -> false
            }


        }

        // Initialize croppedImageView
        croppedImageView = view.findViewById(R.id.croppedImageView)

        // Load the 3D model
        ModelRenderable.builder()
            .setSource(context, Uri.parse("file:///android_asset/models/realmagnifying.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable -> modelRenderable = renderable }
            .exceptionally { throwable ->
                Log.e("MainFragment", "Unable to load Renderable.", throwable)
                null
            }

    }



    private var lastTextResult: Text? = null
    private var lastImageWidth: Int = 0
    private var lastImageHeight: Int = 0

    private fun checkDetectedPLaneForEntry() {
        arSceneView.scene.addOnUpdateListener {
            if (hasPlaneBeenDetectedOnce) return@addOnUpdateListener  // Do nothing if already triggered once

            instructionLabel.visibility = View.VISIBLE
            startRepeatingPing()

            val frame = arSceneView.arFrame ?: return@addOnUpdateListener
            val planes = frame.getUpdatedTrackables(Plane::class.java)

            val isPlaneDetected = planes.any { it.trackingState == TrackingState.TRACKING }

            if (isPlaneDetected) {
                hasPlaneBeenDetectedOnce = true  // Set flag so this block never runs again

                /*instructionLabel.visibility = View.GONE*/
                instructionLabel.text = "Hold the magnifying glass button."
                stopRepeatingPing()

                // Ensure the text recognition result is available
                if (lastTextResult != null) {
                    processTextRecognitionResult(lastTextResult!!, lastImageWidth, lastImageHeight)
                }

                magnifyingGlassButton.visibility = View.VISIBLE
                restartButton.visibility = View.VISIBLE

                Log.d("MainFragment", "Plane detected for the first time – buttons are now visible.")
            }
        }
    }

    private fun setupRecognizableModelNames() {
        val modelDao = AppDatabase.getDatabase(requireContext(), CoroutineScope(Dispatchers.IO)).modelDao()

        // Observe the model names from the database
        modelDao.getAllModelNames().observe(viewLifecycleOwner) { modelNames ->
            highlightOverlayView.recognizableModelNames = modelNames.map { it.lowercase() }.toSet()
            Log.d("MainFragment", "Recognizable model names updated: $modelNames")
        }
    }
    private fun processTextRecognitionResult(result: Text, imageWidth: Int, imageHeight: Int) {
        // Pass the textBlocks from the result to updateTextResult
        highlightOverlayView.updateTextResult(result.textBlocks, imageWidth, imageHeight)
    }

    private fun showMagnifyingGlass() {
        equip.start()
        croppedImageView.visibility = View.VISIBLE

        croppedImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        croppedImageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = 700 // Adjust this value to move up(decrease) or down (increase)
        }


        if (magnifyingGlassNode == null) {
            magnifyingGlassNode = Node().apply {
                setParent(arFragment.arSceneView.scene.camera)
                localPosition = Vector3(0.0f, -0.1f, -0.3f)
                renderable = modelRenderable
            }
        } else {
            magnifyingGlassNode?.setParent(arFragment.arSceneView.scene.camera)
        }
        magnifyingGlassNode?.isEnabled = true
        instructionLabel.visibility = View.VISIBLE
        instructionLabel.text = "Hold to Render"
    }

    private fun hideMagnifyingGlass() {
        resetTextRecognition()
        unequip.start()
        croppedImageView.visibility = View.GONE

        magnifyingGlassNode?.setParent(null)
        magnifyingGlassNode?.isEnabled = false

        instructionLabel.visibility = View.VISIBLE
        instructionLabel.text = "Search for highlighted words."

    }

    private fun preloadModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            modelInfoMap.values.forEach { modelEntity ->
                models[modelEntity.name] = ModelRenderable.builder()
                    .setSource(context, Uri.parse(modelEntity.modelPath))
                    .setIsFilamentGltf(true)
                    .await()

                modelViews[modelEntity.name] = modelEntity.layoutResId?.let {
                    ViewRenderable.builder()
                        .setView(context, it)
                        .await()
                }
            }
        }
    }


    private fun highlightRecognizedWords(
        arSceneView: ArSceneView,
        highlightOverlayView: HighlightOverlayView,
        textRecognizer: TextRecognizer
    ) {
        var lastProcessingTimeMs = 0L
        val minProcessingIntervalMs = 500 // Debounce OCR calls

        arSceneView.scene.addOnUpdateListener { frameTime ->
            val currentTimeMs = System.currentTimeMillis()
            if (currentTimeMs - lastProcessingTimeMs < minProcessingIntervalMs) return@addOnUpdateListener

            val frame = arSceneView.arFrame ?: return@addOnUpdateListener
            if (frame.camera.trackingState != TrackingState.TRACKING) return@addOnUpdateListener

            lifecycleScope.launch(Dispatchers.IO) {
                var image: Image? = null
                try {
                    image = frame.acquireCameraImage() ?: return@launch
                    val (croppedImage, croppedBitmap) = cropToViewfinder(image)
                    val imageWidth = croppedImage.width
                    val imageHeight = croppedImage.height

                    withContext(Dispatchers.Main) {
                        highlightOverlayView.clearOverlay()
                        croppedImageView.setImageBitmap(croppedBitmap) // Display cropped image for debugging
                    }

                    textRecognizer.process(croppedImage)
                        .addOnSuccessListener { visionText ->
                            if (visionText.textBlocks.isEmpty()) {
                                Log.d("HighlightWords", "No text blocks recognized.")
                                return@addOnSuccessListener
                            }

                            val currentRecognizableNames = highlightOverlayView.recognizableModelNames
                            val filteredTextBlocks = visionText.textBlocks.filter { block ->
                                block.lines.any { line ->
                                    line.elements.any { element ->
                                        currentRecognizableNames.any { modelName ->
                                            element.text.lowercase() == modelName
                                        }
                                    }
                                }
                            }

                            if (filteredTextBlocks.isNotEmpty()) {
                                Log.d("HighlightWords", "Recognized words: ${filteredTextBlocks.joinToString { it.text }}")
                                lifecycleScope.launch {
                                    withContext(Dispatchers.Main) {
                                        highlightOverlayView.updateTextResult(filteredTextBlocks, imageWidth, imageHeight)
                                    }
                                }
                            } else {
                                Log.d("HighlightWords", "No matching words found in the database.")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("HighlightWords", "Text recognition failed: ${e.message}")
                        }
                    lastProcessingTimeMs = currentTimeMs
                } catch (e: Exception) {
                    Log.e("HighlightWords", "Error processing frame.", e)
                } finally {
                    image?.close()
                }
            }
        }
    }


    private fun setupTextRecognition(arSceneView: ArSceneView) {
        var lastProcessingTimeMs = 0L
        val minProcessingIntervalMs = 500 // Check twice per second
        Log.d("ModelRecognition", "Setting up listener for model name detection and rendering.")

        arSceneView.scene.addOnUpdateListener { frameTime ->
            if (!isTextRecognitionActive || isRecognitionCancelled) return@addOnUpdateListener

            val currentTimeMs = System.currentTimeMillis()
            if (currentTimeMs - lastProcessingTimeMs < minProcessingIntervalMs) {
                return@addOnUpdateListener
            }

            val frame = arSceneView.arFrame ?: return@addOnUpdateListener
            if (frame.camera.trackingState != TrackingState.TRACKING) {
                return@addOnUpdateListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    frame.acquireCameraImage().use { image ->
                        val (croppedImage, croppedBitmap) = cropToViewfinder(image)
                        withContext(Dispatchers.Main) {
                            croppedImageView.setImageBitmap(croppedBitmap)
                        }
                        textRecognizer.process(croppedImage)
                            .addOnSuccessListener { visionText ->
                                val recognizedText = visionText.text.lowercase()
                                for (modelName in recognizableModelNames) {
                                    if (recognizedText.contains(modelName.lowercase())) {
                                        Log.d("ModelRecognition", "Model name '$modelName' found in text.")
                                        instructionLabel.visibility = View.VISIBLE
                                        instructionLabel.text = "Hold to render 3D Model"
                                        recognizeTextThenRenderModel(modelName, currentTimeMs)
                                        break
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("ModelRecognition", "Text recognition for model detection failed", exception)
                            }
                            .addOnCompleteListener {
                                // No need for image.close() here anymore
                                Log.v("ModelRecognition", "Model detection text processing complete.")
                            }
                    }
                    lastProcessingTimeMs = currentTimeMs
                } catch (e: IllegalStateException) {
                    Log.w("ModelRecognition", "Could not acquire image for model detection: ${e.message}")
                } catch (e: Exception) {
                    Log.e("ModelRecognition", "Error processing frame for model detection", e)
                }
            }
        }
    }

    private fun recognizeTextThenRenderModel(modelName: String, currentTimeMs: Long) {
        // Monitor cancellation during riser playback
        if (isRecognitionCancelled) {
            cancelTextRecognition()

            return
        }

        // Start playing the riser sound
        riserSound()
        riser.setOnCompletionListener {
            if (!isModelPlaced) {
                powerdown.release()
                vibrate()
                startRepeatingPing() // Start repeating ping
                if (currentTimeMs - lastToastTime > TOAST_COOLDOWN_MS) {
                    showToast("'$modelName' detected! Find a Surface to Render the model.")
                    Log.d("TextRecognition", "Model detected: $modelName")
                    lastToastTime = currentTimeMs
                }
                hideMagnifyingGlass()
                view?.findViewById<ImageButton>(R.id.magnifyingGlassButton)?.visibility = View.GONE
                renderModelOnSurface(modelName)
            }
        }
    }

    private fun resetTextRecognition() {
        isRecognitionCancelled = true
        isRiserCancelled = true

        if (::riser.isInitialized) {
            try {
                if (riser.isPlaying) {
                    riser.stop()
                }
            } catch (e: IllegalStateException) {
                Log.e("MainFragment", "MediaPlayer is in an invalid state during reset.", e)
            }
            riser.release()
        }

        // Reinitialize the MediaPlayer for future use
        riser = MediaPlayer.create(context, R.raw.riser)

        // Reset flags back to false after a short delay
        lifecycleScope.launch {
            delay(500) // Adjust delay as needed
            isRecognitionCancelled = false
            isRiserCancelled = false
        }
    }

    private fun cancelTextRecognition(){
        isRiserCancelled = true
        if (::riser.isInitialized) {
            try {
                if (riser.isPlaying) {
                    riser.stop()
                }
                if (!isModelPlaced) {
                    powerdown.start()
                    magnifyingGlassButton.visibility = View.GONE
                    powerdown.setOnCompletionListener{
                        magnifyingGlassButton.visibility = View.VISIBLE
                    }
                }
                riser.release()
                riser = MediaPlayer.create(context, R.raw.riser) // Reinitialize
            } catch (e: IllegalStateException) {
                Log.e("TextRecognition", "MediaPlayer is in an invalid state.", e)
            }
        }
    }

    private fun startTextRecognition() {
        isTextRecognitionActive = true
    }

    private fun stopTextRecognition() {
        isTextRecognitionActive = false
    }

    private fun cropToViewfinder(image: Image): Pair<InputImage, Bitmap> {
        val bitmap = MediaImageToBitmapHelper.convert(image)

        val width = bitmap.width
        val height = bitmap.height

        // Define zoom factor ( 6.0 means 6x zoom)
        val zoomFactor = 6.0f

        // Crop a smaller region from center to simulate zoom
        val cropSize = (Math.min(width, height) / zoomFactor).toInt()
        val centerX = width / 2
        val centerY = height / 2
        val left = (centerX - cropSize / 2).coerceAtLeast(0)
        val top = (centerY - cropSize / 2).coerceAtLeast(0)

        var croppedBitmap = Bitmap.createBitmap(bitmap, left, top, cropSize, cropSize)

        // Scale it up to simulate zoom
        val zoomedBitmap = Bitmap.createScaledBitmap(croppedBitmap, cropSize * zoomFactor.toInt(), cropSize * zoomFactor.toInt(), true)

        // Rotate after scaling
        val matrix = Matrix().apply { postRotate(90f) }
        val rotatedZoomedBitmap = Bitmap.createBitmap(
            zoomedBitmap, 0, 0,
            zoomedBitmap.width, zoomedBitmap.height,
            matrix, true
        )

        // Optional: mask into a circular preview
        val circularBitmap = Bitmap.createBitmap(rotatedZoomedBitmap.width, rotatedZoomedBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(circularBitmap)
        val paint = Paint().apply { isAntiAlias = true }
        val rect = Rect(0, 0, rotatedZoomedBitmap.width, rotatedZoomedBitmap.height)
        val rectF = RectF(rect)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(rotatedZoomedBitmap, rect, rect, paint)

        val inputImage = InputImage.fromBitmap(circularBitmap, 0)
        return Pair(inputImage, circularBitmap)
    }


private fun renderModelOnSurface(modelName: String) {
    val modelEntity = modelInfoMap[modelName] ?: return // Get model from DB

    // Check if the model is already loaded
    if (models[modelName] == null || modelViews[modelName] == null) {
        vibrate()
        pingSound()
        Toast.makeText(context, "Loading model...", Toast.LENGTH_SHORT).show()

        // Dynamically load the model
        lifecycleScope.launch(Dispatchers.Main) { // Ensure this runs on the Main thread
            try {
                val model = ModelRenderable.builder()
                    .setSource(context, Uri.parse(modelEntity.modelPath))
                    .setIsFilamentGltf(true)
                    .await()

                val modelView = modelEntity.layoutResId?.let {
                    ViewRenderable.builder()
                        .setView(context, it)
                        .await()
                }

                models[modelName] = model
                modelViews[modelName] = modelView

                // Retry rendering after loading
                renderModelOnSurface(modelName)
            } catch (e: Exception) {
                Log.e("ModelLoading", "Failed to load model: $modelName", e)
            }
        }
        return
    }

    // Render the model on a detected surface
    arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
        val frame = arFragment.arSceneView.arFrame ?: return@addOnUpdateListener
        val planes = frame.getUpdatedTrackables(Plane::class.java)

        for (plane in planes) {
            if (plane.trackingState == TrackingState.TRACKING && !isModelPlaced) {
                val pose = plane.centerPose
                val anchor = plane.createAnchor(pose)
                placeModel(anchor, modelEntity)
                isModelPlaced = true

                // Ensure button visibility is updated on the main thread
                activity?.runOnUiThread {
                    if (modelEntity.interactionVideoResId != null) { // Check if a video resource ID exists
                        watchButton.visibility = View.VISIBLE
                        watchButton.bringToFront() // Bring to front if needed
                    } else {
                        watchButton.visibility = View.GONE // Hide the button if no video is available
                    }
                }

                // Set up the watch button once the model is placed
                setupWatchButton(modelEntity)
                setupCloseButton()

                break
            }
        }
    }
}

    private fun setupWatchButton(modelEntity: ModelEntity) {
        watchButton.setOnClickListener {
            if (exoPlayer == null) {
                val uri = Uri.parse("android.resource://${requireContext().packageName}/${modelEntity.interactionVideoResId}")
                exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
                    setMediaItem(MediaItem.fromUri(uri))
                    prepare()
                    play()
                }
            } else if (exoPlayer?.isPlaying == false) {
                exoPlayer?.play()
            }

            // Assign player and update UI
            playerView.player = exoPlayer
            videoOverlayContainer.visibility = View.VISIBLE

            // UI Toggles
            watchButton.visibility = View.GONE
            closeButton.visibility = View.VISIBLE

            arFragment.arSceneView.pause()
        }

        setupFullscreenToggle()

    }

    private fun setupCloseButton() {
        closeButton.setOnClickListener {
            // UI Toggles
            closeButton.visibility = View.GONE
            watchButton.visibility = View.VISIBLE

            videoOverlayContainer.visibility = View.GONE
            exoPlayer?.pause()
            exoPlayer?.seekTo(0)
            arFragment.arSceneView.resume()
        }
    }

    private fun setupFullscreenToggle() {
        fullscreenToggle.setOnClickListener {
            if (isFullscreen) {
                exitFullscreen()
            } else {
                enterFullscreen()
            }
        }
    }

    private fun enterFullscreen() {
        // Change to landscape mode
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Make the container fill the screen
        videoOverlayContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        videoOverlayContainer.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        // Update PlayerView size to fill the container
        val playerViewLayoutParams = playerView.layoutParams
        playerViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        playerViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        playerView.layoutParams = playerViewLayoutParams

        // Hide system UI for fullscreen effect
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        // Hide UI buttons when in full screen
        closeButton.visibility = View.GONE
        infoButton.visibility = View.GONE
        restartButton.visibility = View.GONE
        switchButton.visibility = View.GONE
        libraryBackButton.visibility = View.GONE

        fullscreenToggle.setImageResource(R.drawable.fullscreen_exit) // your "exit fullscreen" icon

        // Change the fullscreen flag state
        isFullscreen = true
    }

    private fun exitFullscreen() {
        // Restore to portrait mode
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // First, reset the layout params for videoOverlayContainer
        videoOverlayContainer.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        videoOverlayContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        videoOverlayContainer.requestLayout()


        // Now, reset PlayerView to its minimized size
        playerView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        playerView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        playerView.requestLayout()

        playerView.requestLayout()  // Ensure PlayerView is remeasured properly

        // Show system UI again
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        fullscreenToggle.setImageResource(R.drawable.fullscreen) // your "fullscreen" icon

        // Change the fullscreen flag state
        isFullscreen = false

        // Restore button visibility after layout adjustments (if needed)
        closeButton.visibility = View.VISIBLE
        infoButton.visibility = View.VISIBLE
        restartButton.visibility = View.VISIBLE
        switchButton.visibility = View.VISIBLE
        libraryBackButton.visibility = View.VISIBLE
    }



    private fun placeModel(anchor: Anchor, modelEntity: ModelEntity) {
        val modelName = modelEntity.name
        val model = models[modelName] ?: return
        val modelView = modelViews[modelName] ?: return


        scene.addChild(AnchorNode(anchor).apply {
            addChild(TransformableNode(arFragment.transformationSystem).apply {
                renderable = model
                renderableInstance.setCulling(false)
                renderableInstance.animate(true).start()

                // Add InfoNode
                addChild(Node().apply {
                    name = "InfoNode"
                    localPosition = Vector3(0.0f, 1f, 0.0f)
                    localScale = Vector3(0.7f, 0.7f, 0.7f)
                    renderable = modelView
                    isEnabled = false
                })

                // Tap to trigger sound
                setOnTapListener { _, _ ->
                    playInteractionSound(modelEntity.interactionSoundResId)
                }
            })
        })

        playRenderSound()
        stopRepeatingPing()
        isModelPlaced = true
        infoButton.visibility = View.VISIBLE
        instructionLabel.visibility = View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }
    }

    private fun toggleInfoVisibility() {
        isInfoVisible = !isInfoVisible
        scene.findByName("InfoNode")?.let { infoNode ->
            infoNode.isEnabled = isInfoVisible
            if (isInfoVisible) {
                onSound()
            } else {
                offSound()
            }
        }
        vibrate()
    }

    private fun playInteractionSound(soundResId: Int) {
        MediaPlayer.create(context, soundResId)?.apply {
            setOnCompletionListener { release() }
            start()
        }
    }

    private fun playRenderSound() {
        mediaPlayer.start()
    }

    private fun pingSound() {
        ping.start()
    }

    private fun startRepeatingPing() {
        // Cancel any existing ping job
        pingJob?.cancel()

        pingJob = lifecycleScope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastPingTime >= PING_DEBOUNCE_TIME) {
                    pingSound()
                    lastPingTime = currentTime
                }
                delay(PING_DEBOUNCE_TIME)
            }
        }
    }

    private fun stopRepeatingPing() {
        pingJob?.cancel()
        pingJob = null
        lastPingTime = 0L
    }

    private fun onSound() {
        on.start()
    }

    private fun offSound() {
        off.start()
    }

    private fun riserSound(){
        if (::riser.isInitialized) {
            try {
                riser.start()
            } catch (e: IllegalStateException) {
                Log.e("TextRecognition", "Failed to start MediaPlayer.", e)
                riser.release()
                riser = MediaPlayer.create(context, R.raw.riser) // Reinitialize
            }
        }
    }

    private fun unloadModels() {
        models.clear()
        modelViews.clear()
        modelRenderable = null
    }

    private fun switchToReciteFragment() {
        unloadModels()
        parentFragmentManager.beginTransaction()
            .replace(R.id.containerFragment, ReciteFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        on.release()
        off.release()
        riser.release()
        powerdown.release()
        equip.release()
        unequip.release()
        exoPlayer?.release()
    }


}