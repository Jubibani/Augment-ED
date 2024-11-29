package com.example.augment_ed.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ConceptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: Concept)

    @Query("SELECT * FROM concept_table WHERE term = :term LIMIT 1")
    suspend fun getConceptByTerm(term: String): Concept?

    @Query("SELECT COUNT(*) FROM concept_table")
    suspend fun getConceptCount(): Int
    abstract fun insert(concept: Concept)
    abstract fun getRandomConcept(): Concept?


    @Query("SELECT * FROM concept_table ORDER BY RANDOM() LIMIT 1")
    abstract fun getRandomConceptFromDatabase(): Concept?
}