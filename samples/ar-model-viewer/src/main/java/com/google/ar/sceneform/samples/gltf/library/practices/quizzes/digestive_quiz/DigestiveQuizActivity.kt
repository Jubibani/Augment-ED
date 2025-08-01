package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.digestive_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.samples.gltf.R

class DigestiveQuizActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.organism_quiz_activity)

        val etName = findViewById<EditText>(R.id.etName)
        val btnStart = findViewById<Button>(R.id.btnStart)



        btnStart.setOnClickListener {
            // Directly start the quiz without requiring a name
            val intent = Intent(this, DigestiveQuizQuestion::class.java)
            startActivity(intent)
        }
    }
}