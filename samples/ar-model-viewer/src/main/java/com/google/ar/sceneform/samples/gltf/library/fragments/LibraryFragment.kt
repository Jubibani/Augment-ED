package com.google.ar.sceneform.samples.gltf.library.fragments

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity
import com.google.ar.sceneform.samples.gltf.library.screens.LibraryActivity
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private lateinit var arFragment: ArFragment
    private lateinit var modelDao: ModelDao
    private var modelName: String? = null
    private var model: ModelRenderable? = null
    private var isModelPlaced = false
    private var hasPlaneBeenDetected = false
    private var mediaPlayer: MediaPlayer? = null
    private var currentModel: ModelEntity? = null
    private var isInfoVisible = false
    private var infoNode: Node? = null
    private lateinit var onSound: MediaPlayer
    private lateinit var offSound: MediaPlayer
    private lateinit var backSound: MediaPlayer
    private lateinit var infoButton: FloatingActionButton
    private lateinit var backButton: FloatingActionButton
    private lateinit var refreshButton: FloatingActionButton
    private lateinit var refreshSound: MediaPlayer
    private lateinit var magnifyingGlassButton: ImageButton
    private lateinit var instructionLabel: TextView
    private var anchorNode: AnchorNode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        instructionLabel = view.findViewById(R.id.instructionLabel)
        modelName = arguments?.getString("modelName")
        modelDao = AppDatabase.getDatabase(requireContext(), lifecycleScope).modelDao()
        arFragment = childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        infoButton = view.findViewById(R.id.infoButton)
        magnifyingGlassButton = view.findViewById(R.id.magnifyingGlassButton)
        refreshButton = view.findViewById(R.id.refreshButton)
        backButton = view.findViewById(R.id.libraryBackButton)
        refreshSound = MediaPlayer.create(requireContext(), R.raw.refresh)
        backSound = MediaPlayer.create(requireContext(), R.raw.back)
        onSound = MediaPlayer.create(requireContext(), R.raw.on)
        offSound = MediaPlayer.create(requireContext(), R.raw.off)

        // Initially hide the buttons
        refreshButton.visibility = View.GONE
        backButton.visibility = View.GONE
        magnifyingGlassButton.visibility = View.GONE

        // Delay the appearance of the back button
        Handler(Looper.getMainLooper()).postDelayed({
            backButton.visibility = View.VISIBLE
        }, 2000)

        refreshButton.setOnClickListener { refreshFragment() }

        backButton.setOnClickListener {
            backSound.start()
            backSound.setOnCompletionListener {
                navigateBack()
            }
        }

        infoButton.setOnClickListener { toggleInfo() }
        infoButton.visibility = View.GONE  // Initially hidden
        instructionLabel.visibility = View.VISIBLE

        modelName?.let { loadModel(it) }
        setupTapToRender()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        waitForArSceneViewThenSetupPlaneDetection()
    }

    private fun waitForArSceneViewThenSetupPlaneDetection() {
        // Use ViewTreeObserver to ensure arSceneView is ready
        val arSceneView = arFragment.view
        if (arSceneView != null) {
            arSceneView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (arFragment.arSceneView != null) {
                        setupPlaneDetectionInstructions()
                        arSceneView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
        } else {
            // Fallback: post it to the main thread if arFragment.view is not ready yet
            Handler(Looper.getMainLooper()).postDelayed({
                waitForArSceneViewThenSetupPlaneDetection()
            }, 50)
        }
    }

    private fun setupPlaneDetectionInstructions() {
        // Defensive: check arSceneView before using
        val arSceneView = arFragment.arSceneView ?: return
        arSceneView.scene.addOnUpdateListener {
            if (isModelPlaced) {
                instructionLabel.visibility = View.GONE
                return@addOnUpdateListener
            }

            val frame = arSceneView.arFrame
            val planes = frame?.getUpdatedTrackables(com.google.ar.core.Plane::class.java)
            val anyPlaneTracking = planes?.any { it.trackingState == TrackingState.TRACKING } == true

            if (anyPlaneTracking && !hasPlaneBeenDetected) {
                instructionLabel.text = "Tap a white dot to place the 3D model."
                instructionLabel.visibility = View.VISIBLE
                hasPlaneBeenDetected = true
            } else if (!anyPlaneTracking && !hasPlaneBeenDetected) {
                instructionLabel.text = "Move your device to detect a surface."
                instructionLabel.visibility = View.VISIBLE
            }
        }
    }

    private fun loadModel(modelName: String) {
        lifecycleScope.launch {
            currentModel = modelDao.getModelByName(modelName)
            Log.d("LibraryFragment", "Retrieved Model from DB: $currentModel")

            currentModel?.let { model ->
                val modelPath = model.modelPath
                val soundResId = model.interactionSoundResId

                mediaPlayer = MediaPlayer.create(requireContext(), soundResId)

                ModelRenderable.builder()
                    .setSource(requireContext(), Uri.parse(modelPath))
                    .setIsFilamentGltf(true)
                    .build()
                    .thenAccept { modelRenderable ->
                        this@LibraryFragment.model = modelRenderable
                        Log.d("LibraryFragment", "Model loaded: $modelName")
                        infoNode?.setParent(null)
                        infoNode = null
                        createInfoNode()
                    }
                    .exceptionally { throwable ->
                        Log.e("LibraryFragment", "Error loading model: ", throwable)
                        null
                    }
            }
        }
    }

    private fun setupTapToRender() {
        instructionLabel.visibility = View.VISIBLE

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if (isModelPlaced) return@setOnTapArPlaneListener

            val model = this.model ?: return@setOnTapArPlaneListener
            val anchor = hitResult.createAnchor()
            placeModel(anchor, model)
            isModelPlaced = true

            instructionLabel.visibility = View.GONE
            infoButton.visibility = View.VISIBLE
        }
    }

    private fun placeModel(anchor: Anchor, modelRenderable: ModelRenderable) {
        anchorNode = AnchorNode(anchor)
        anchorNode?.setParent(arFragment.arSceneView.scene)

        val transformableNode = TransformableNode(arFragment.transformationSystem)
        transformableNode.setParent(anchorNode)
        transformableNode.renderable = modelRenderable
        transformableNode.localPosition = Vector3(0.0f, 0.0f, 0.0f)
        transformableNode.select()

        mediaPlayer?.start()

        transformableNode.setOnTapListener { _, _ -> mediaPlayer?.start() }

        createInfoNode()
        refreshButton.visibility = View.VISIBLE
    }

    private fun toggleInfo() {
        if (infoNode == null) {
            Log.e("LibraryFragment", "Info node is null, cannot toggle info")
            return
        }

        isInfoVisible = !isInfoVisible
        infoNode?.isEnabled = isInfoVisible

        if (isInfoVisible) {
            onSound.start()
            Log.d("LibraryFragment", "Info shown")
        } else {
            offSound.start()
            Log.d("LibraryFragment", "Info hidden")
        }
    }

    private fun createInfoNode() {
        if (anchorNode == null) {
            Log.e("LibraryFragment", "AnchorNode is null, cannot create info node")
            Log.d("LibraryFragment", "Retrieved Info from DB: $anchorNode")
            return
        }

        currentModel?.let { model ->
            Log.d("LibraryFragment", "Creating info node for ${model.name}")

            model.layoutResId?.let {
                ViewRenderable.builder()
                    .setView(context, it)
                    .build()
                    .thenAccept { viewRenderable: ViewRenderable ->
                        infoNode?.setParent(null)
                        infoNode = Node().apply {
                            setParent(anchorNode)
                            localPosition = Vector3(0.0f, 1f, 0.0f)
                            localScale = Vector3(0.7f, 0.7f, 0.7f)
                            renderable = viewRenderable
                            isEnabled = false
                        }
                        Log.d("LibraryFragment", "Info node created successfully")
                        Log.d("LibraryFragment", "Retrieved Info from DB: $anchorNode")
                    }
                    .exceptionally { throwable ->
                        Log.e("LibraryFragment", "Error creating info view: ", throwable)
                        null
                    }
            }
        } ?: Log.e("LibraryFragment", "currentModel is null, cannot create info node")
    }

    private fun navigateBack() {
        val intent = Intent(requireActivity(), LibraryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun refreshFragment() {
        refreshSound.start()

        // Remove the existing model
        anchorNode?.setParent(null)
        anchorNode = null

        // Reset the info node
        infoNode?.setParent(null)
        infoNode = null

        // Reset the model placement flag and plane detection
        isModelPlaced = false
        hasPlaneBeenDetected = false

        // Hide the info button and info
        infoButton.visibility = View.GONE
        isInfoVisible = false

        // Hide the refresh button
        refreshButton.visibility = View.GONE

        // Show instruction label again
        instructionLabel.visibility = View.VISIBLE
        instructionLabel.text = "Move your device to detect a surface."

        // Reload the model
        modelName?.let { loadModel(it) }

        // Re-setup the tap to render functionality
        setupTapToRender()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        onSound.release()
        offSound.release()
        backSound.release()
    }
}