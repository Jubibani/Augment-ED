package com.example.augment_ed.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: Concept)

    @Query("SELECT * FROM concept_table WHERE term = :term LIMIT 1")
     fun getConceptByTerm(term: String): Concept?


    @Query("SELECT * FROM concept_table WHERE term = :term LIMIT 1")
    suspend fun getConceptByName(term: String): Concept?

    @Query("SELECT COUNT(*) FROM concept_table")
    fun getConceptCount(): Flow<Int>

    @Query("SELECT * FROM concept_table ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomConceptFromDatabase(): Concept?

}