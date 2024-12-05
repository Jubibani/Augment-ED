package com.google.ar.core.examples.kotlin.helloar

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.sceneform.SceneView

class HelloArView(private val activity: HelloArActivity) : DefaultLifecycleObserver {

  val root: View = View.inflate(activity, R.layout.activity_main, null)
  val sceneView: SceneView = root.findViewById(R.id.scene_view)

  // No need for onResume() or onPause()
  override fun onResume(owner: LifecycleOwner) {
    // SceneView manages its own lifecycle. No need for explicit calls.
  }

  override fun onPause(owner: LifecycleOwner) {
    // SceneView manages its own lifecycle. No need for explicit calls.
  }
}