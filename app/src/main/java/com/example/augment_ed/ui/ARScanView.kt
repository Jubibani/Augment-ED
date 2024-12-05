package com.example.augment_ed.ui

import ARViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions



class CustomArFragment : Fragment() {
    private lateinit var arFragment: ArFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arFragment = ArFragment()
        return arFragment.onCreateView(inflater, container, savedInstanceState)
    }

    fun getArFragment(): ArFragment = arFragment
}

@Composable
fun ARScanView(viewModel: ARViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val customArFragment = remember { CustomArFragment() }
    var isScanning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadConcepts(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                customArFragment.getArFragment().arSceneView.session?.let { session ->
                    val config = Config(session)
                    config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    config.focusMode = Config.FocusMode.AUTO
                    session.configure(config)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                FragmentContainerView(context).apply {
                    id = View.generateViewId()
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                (view.parent as? ViewGroup)?.let { parent ->
                    val fragmentManager = (context as androidx.fragment.app.FragmentActivity).supportFragmentManager
                    fragmentManager.beginTransaction()
                        .replace(view.id, customArFragment)
                        .commit()

                    customArFragment.getArFragment().setOnTapArPlaneListener { hitResult, plane, motionEvent ->
                        if (isScanning) {
                            val image = customArFragment.getArFragment().arSceneView.arFrame?.acquireCameraImage()
                            image?.let {
                                val inputImage = InputImage.fromMediaImage(it, 0)
                                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                recognizer.process(inputImage)
                                    .addOnSuccessListener { visionText ->
                                        val recognizedText = visionText.text
                                        viewModel.recognizeTerm(recognizedText)
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle any errors
                                    }
                                it.close()
                            }
                        } else {
                            viewModel.currentConcept.value?.let { concept ->
                                viewModel.loadModelForConcept(context) { modelRenderable ->
                                    val anchor = hitResult.createAnchor()
                                    val anchorNode = AnchorNode(anchor)
                                    anchorNode.setParent(customArFragment.getArFragment().arSceneView.scene)

                                    val transformableNode = TransformableNode(customArFragment.getArFragment().transformationSystem)
                                    transformableNode.setParent(anchorNode)
                                    transformableNode.renderable = modelRenderable
                                    transformableNode.select()
                                }
                            }
                        }
                    }
                }
            }
        )
        Button(
            onClick = { isScanning = !isScanning },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(text = if (isScanning) "Stop Scanning" else "Start Scanning")
        }
    }
}