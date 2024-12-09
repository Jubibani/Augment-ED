package com.google.ar.core.examples.kotlin.common.helpers

import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognizeText(inputImage: InputImage, onResult: (Text) -> Unit) {
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText -> onResult(visionText) }
            .addOnFailureListener { e ->
                Log.e("TextRecognitionHelper", "Text recognition failed: ${e.message}")
            }
    }
}