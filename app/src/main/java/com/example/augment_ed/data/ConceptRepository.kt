package com.example.augment_ed.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConceptRepository(private val conceptDao: ConceptDao) {
    suspend fun getConceptByTerm(term: String): Concept? = withContext(Dispatchers.IO) {
        conceptDao.getConceptByTerm(term)
    }

    suspend fun insertConcept(concept: Concept) = withContext(Dispatchers.IO) {
        conceptDao.insert(concept)
    }
}