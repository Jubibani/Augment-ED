package com.google.ar.core.examples.kotlin.helloar

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.sceneform.SceneView

class HelloArView(private val activity: HelloArActivity) : DefaultLifecycleObserver {

  // Root view layout for the AR experience
  val root: View = LayoutInflater.from(activity).inflate(R.layout.activity_main, null)

  // SceneView for rendering ARCore content
  val sceneView: SceneView = root.findViewById(R.id.scene_view)

  // Resume the AR scene when the activity is resumed
  override fun onResume(owner: LifecycleOwner) {
    sceneView.resume()
  }

  // Pause the AR scene when the activity is paused
  override fun onPause(owner: LifecycleOwner) {
    sceneView.pause()
  }
}