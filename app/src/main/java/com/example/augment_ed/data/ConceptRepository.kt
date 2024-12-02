package com.example.augment_ed.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ConceptRepository(private val conceptDao: ConceptDao) {
    fun getConceptByTerm(term: String): Flow<Concept?> {
        return conceptDao.getConceptByTerm(term)
    }

    suspend fun getRandomConcept(): Concept? {
        return conceptDao.getRandomConceptFromDatabase().firstOrNull()
    }

    suspend fun insertConcept(concept: Concept) {
        conceptDao.insertConcept(concept)
    }

    fun getConceptCount(): Flow<Int> {
        return conceptDao.getConceptCount()
    }
}