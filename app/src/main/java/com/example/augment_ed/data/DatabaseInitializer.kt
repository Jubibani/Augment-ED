package com.example.augment_ed.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseInitializer(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val conceptDao = database.conceptDao()

    suspend fun initializeDatabase() {
        withContext(Dispatchers.IO) {
            if (conceptDao.getConceptCount() == 0) {
                val concepts = loadConceptsFromJson()
                concepts.forEach { concept ->
                    conceptDao.insertConcept(concept)
                }
            }
        }
    }

    private fun loadConceptsFromJson(): List<Concept> {
        val jsonString = context.assets.open("initial_concepts.json").bufferedReader().use { it.readText() }
        val conceptListType = object : TypeToken<List<Concept>>() {}.type
        return Gson().fromJson(jsonString, conceptListType)
    }
}