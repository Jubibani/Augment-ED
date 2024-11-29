package com.example.augment_ed.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.augment_ed.data.ConceptRepository

class ARViewModelFactory(private val repository: ConceptRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ARViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ARViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}