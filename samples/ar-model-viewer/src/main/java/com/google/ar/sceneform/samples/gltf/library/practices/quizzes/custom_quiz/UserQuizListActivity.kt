package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.custom_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UserQuizListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listView = ListView(this)
        setContentView(listView)

        val quizzes = InMemoryQuizRepo.quizzes
        if (quizzes.isEmpty()) {
            Toast.makeText(this, "No quizzes found. Create one first.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, CreateQuizActivity::class.java))
            finish()
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, quizzes.map { it.title })
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, UserQuizAttemptActivity::class.java)
            intent.putExtra("QUIZ_INDEX", position)
            startActivity(intent)
        }
    }
}