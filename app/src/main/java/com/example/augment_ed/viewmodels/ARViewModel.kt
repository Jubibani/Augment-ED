package com.example.augment_ed.viewmodels

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.augment_ed.data.Concept
import com.example.augment_ed.data.ConceptRepository
import com.example.augment_ed.utils.TextRecognitionHelper
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch


class ARViewModel(private val conceptRepository: ConceptRepository) : ViewModel() {
    private val _arState = MutableStateFlow<ARState>(ARState.Idle)
    val arState: StateFlow<ARState> = _arState.asStateFlow()
    private val _currentConcept = MutableStateFlow<Concept?>(null)
    val currentConcept: StateFlow<Concept?> = _currentConcept.asStateFlow()

    fun getConceptByTerm(term: String) {
        viewModelScope.launch {
            _currentConcept.value = conceptRepository.getConceptByTerm(term)
        }
    }

    fun startARScan() {
        _arState.value = ARState.Scanning
    }

    fun processImage(bitmap: Bitmap, context: Context) {
        viewModelScope.launch {
            try {
                val recognizedText = TextRecognitionHelper.recognizeText(bitmap)
                val concept = conceptRepository.getConceptByName(recognizedText)
                if (concept != null) {
                    loadModelForConcept(context, concept.modelPath) { modelRenderable ->
                        _arState.value = ARState.ModelLoaded(modelRenderable)
                    }
                } else {
                    _arState.value = ARState.Error("Concept not found: $recognizedText")
                }
            } catch (e: Exception) {
                _arState.value = ARState.Error("Error processing image: ${e.message}")
            }
        }
    }

    fun loadModelForConcept(context: Context, modelPath: String, onModelLoaded: (ModelRenderable) -> Unit) {
        viewModelScope.launch {
            try {
                val modelRenderable = ModelRenderable.builder()
                    .setSource(context, android.net.Uri.parse(modelPath))
                    .build()
                    .await()
                onModelLoaded(modelRenderable)
            } catch (e: Exception) {
                _arState.value = ARState.Error("Error loading model: ${e.message}")
            }
        }
    }

    suspend fun getRandomConcept(): Concept? {
        return conceptRepository.getRandomConcept()
    }

    sealed class ARState {
        object Idle : ARState()
        object Scanning : ARState()
        data class ModelLoaded(val modelRenderable: ModelRenderable) : ARState()
        data class Error(val message: String) : ARState()
    }
}