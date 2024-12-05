package com.google.ar.core.examples.kotlin.helloar

import android.graphics.Point
import android.opengl.GLES30
import android.util.Log
import com.google.ar.core.*
import com.google.ar.core.examples.java.common.samplerender.Framebuffer
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.examples.java.common.samplerender.Mesh
import com.google.ar.core.examples.java.common.samplerender.Shader
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer
import com.google.ar.core.examples.kotlin.common.helpers.ARCoreSessionLifecycleHelper
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class HelloArRenderer(
    private val activity: HelloArActivity,
    private val sessionHelper: ARCoreSessionLifecycleHelper
) : SampleRender.Renderer {

    private lateinit var backgroundRenderer: BackgroundRenderer
    private lateinit var virtualSceneFramebuffer: Framebuffer
    private lateinit var virtualObjectShader: Shader
    private lateinit var virtualObjectMesh: Mesh

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val keywordToModelMap = mapOf(
        "amphibian" to "models/frog.glb",
        // Add more keyword-model mappings here
    )

    override fun onSurfaceCreated(render: SampleRender) {
        backgroundRenderer = BackgroundRenderer(render)
        virtualSceneFramebuffer = Framebuffer(render, 1, 1)

        virtualObjectShader = Shader.createFromAssets(
            render,
            "shaders/ar_unlit_object.vert",
            "shaders/ar_unlit_object.frag",
            null
        )
        virtualObjectMesh = Mesh.createFromAsset(render, "models/andy.obj")
    }

    override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
        virtualSceneFramebuffer.resize(width, height)
    }

    override fun onDrawFrame(render: SampleRender) {
        val session = sessionHelper.session ?: return
        val frame = session.update()

        backgroundRenderer.updateDisplayGeometry(frame)
        backgroundRenderer.drawBackground(render)

        // Clear depth for 3D object rendering
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)

        // Perform text recognition and place 3D model
        performTextRecognitionAndPlaceModel(frame)
    }

    private fun performTextRecognitionAndPlaceModel(frame: Frame) {
        val cameraImage = frame.acquireCameraImage() ?: return

        val inputImage = InputImage.fromMediaImage(cameraImage, 0)
        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val word = element.text.lowercase()
                            val boundingBox = element.boundingBox ?: continue

                            if (keywordToModelMap.containsKey(word)) {
                                val centerX = boundingBox.centerX().toFloat()
                                val centerY = boundingBox.centerY().toFloat()

                                val hitResult = frame.hitTest(centerX, centerY).firstOrNull()
                                if (hitResult != null) {
                                    val anchor = hitResult.createAnchor()
                                    activity.placeModel(word, anchor)
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("HelloArRenderer", "Text recognition failed", e)
            }
            .addOnCompleteListener {
                cameraImage.close()
            }
    }
}
