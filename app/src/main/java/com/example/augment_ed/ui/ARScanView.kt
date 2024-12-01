package com.example.augment_ed.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.augment_ed.viewmodels.ARViewModel
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

// Custom ArFragment that extends Fragment
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
    )
}