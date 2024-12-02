package com.example.augment_ed.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "concept_table")
data class Concept(
    @PrimaryKey val term: String,
    val description: String,
    val modelPath: String,
    val pronunciationGuide: String?
)