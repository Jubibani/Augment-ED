package com.example.augment_ed.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.augment_ed.viewmodels.ARViewModel
import com.google.ar.core.Config
import com.google.ar.sceneform.ux.ArFragment

@Composable
fun ARScanView(viewModel: ARViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val arFragment = remember { ArFragment() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                arFragment.arSceneView.session?.let { session ->
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
                id = androidx.core.R.id.fragment_container_view_tag
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
                    .replace(view.id, arFragment)
                    .commit()

                arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
                    viewModel.loadModelForConcept(context) { renderable ->
                        val anchorNode = com.google.ar.sceneform.AnchorNode(hitResult.createAnchor())
                        val modelNode = com.google.ar.sceneform.Node().apply {
                            setParent(anchorNode)
                            renderable = renderable
                        }
                        arFragment.arSceneView.scene.addChild(anchorNode)
                    }
                }
            }
        }
    )
}