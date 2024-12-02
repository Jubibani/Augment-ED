package com.example.augment_ed.data

import kotlinx.coroutines.flow.Flow

class ConceptRepository(private val conceptDao: ConceptDao) {
    fun getConceptByTerm(term: String): Concept? {
        return conceptDao.getConceptByTerm(term)
    }

    suspend fun getRandomConcept(): Concept? {
        return conceptDao.getRandomConceptFromDatabase()
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