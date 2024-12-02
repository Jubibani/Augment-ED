package com.example.augment_ed.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.augment_ed.viewmodels.ARViewModel
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    var customArFragment by remember { mutableStateOf<CustomArFragment?>(null) }

    DisposableEffect(Unit) {
        val fragment = CustomArFragment()
        customArFragment = fragment
        onDispose { }
    }

    LaunchedEffect(customArFragment) {
        customArFragment?.let { fragment ->
            fragment.getArFragment().setOnTapArPlaneListener { hitResult: HitResult, _, _ ->
                coroutineScope.launch {
                    val concept = viewModel.getRandomConcept()
                    concept?.let {
                        viewModel.loadModelForConcept(context, it.modelPath) { modelRenderable ->
                            addModelToScene(hitResult, modelRenderable, fragment.getArFragment())
                        }
                    }
                }
            }
        }
    }

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
                customArFragment?.let { fragment ->
                    fragmentManager.beginTransaction()
                        .replace(view.id, fragment)
                        .commit()
                }
            }
        }
    )
}

private fun addModelToScene(hitResult: HitResult, modelRenderable: ModelRenderable, arFragment: ArFragment) {
    val anchor = hitResult.createAnchor()
    val anchorNode = AnchorNode(anchor)
    anchorNode.setParent(arFragment.arSceneView.scene)

    val transformableNode = TransformableNode(arFragment.transformationSystem)
    transformableNode.setParent(anchorNode)
    transformableNode.renderable = modelRenderable
    transformableNode.select()
}