package com.google.ar.sceneform.samples.gltf.library.helpers

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.ar.sceneform.samples.gltf.library.components.TextRecognitionAnalyzer
import com.google.mlkit.vision.text.Text
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraHelper(private val context: Context) {
    private var imageCapture: ImageCapture? = null
    var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService

    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView, onTextRecognized: (Text) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, TextRecognitionAnalyzer(onTextRecognized))
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )

                // Enable auto-focus
                camera?.cameraControl?.enableTorch(false)
                camera?.cameraControl?.setLinearZoom(0f)
                camera?.cameraControl?.enableTorch(false)

            } catch (exc: Exception) {
                Log.e("CameraHelper", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(onPhotoTaken: (Bitmap) -> Unit) {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    onPhotoTaken(bitmap)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraHelper", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}