package com.example.augment_ed.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.augment_ed.data.Concept
import com.example.augment_ed.data.ConceptRepository
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ARViewModel(private val conceptRepository: ConceptRepository) : ViewModel() {
    private val _currentConcept = MutableStateFlow<Concept?>(null)
    val currentConcept: StateFlow<Concept?> = _currentConcept

    fun getConceptByTerm(term: String) {
        viewModelScope.launch {
            _currentConcept.value = conceptRepository.getConceptByTerm(term)
        }
    }
    fun startARScan() {
    // Implement AR scanning logic here
    // For now, let's just simulate scanning by getting a random concept
    viewModelScope.launch {
        val randomConcept = conceptRepository.getRandomConcept()
        _currentConcept.value = randomConcept
    }
}

    fun loadModelForConcept(context: Context, onModelLoaded: (ModelRenderable) -> Unit) {
        val concept = _currentConcept.value ?: return
        val modelPath = concept.modelPath
        val modelUri = android.net.Uri.parse("file:///android_asset/$modelPath")
        
        ModelRenderable.builder()
            .setSource(context, modelUri)
            .build()
            .thenAccept { renderable ->
                onModelLoaded(renderable)
            }
            .exceptionally { throwable ->
                // Handle any errors
                null
            }
    }
}