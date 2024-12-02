package com.example.augment_ed.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ConceptRepository(private val conceptDao: ConceptDao) {
     fun getConceptByTerm(term: String): Concept? {
        return conceptDao.getConceptByTerm(term)
    }

    suspend fun getRandomConcept(term: String): Concept? {
        return conceptDao.getRandomConceptFromDatabase(term)
    }

    suspend fun insertConcept(concept: Concept) {
        conceptDao.insertConcept(concept)
    }

    suspend fun getConceptByName(name: String): Concept? {
        return conceptDao.getConceptByName(name)
    }
    fun getConceptCount(): Flow<Int> {
        return conceptDao.getConceptCount()
    }

}