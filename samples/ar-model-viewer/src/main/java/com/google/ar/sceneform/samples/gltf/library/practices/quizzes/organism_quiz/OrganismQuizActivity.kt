package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.organism_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.samples.gltf.R

class OrganismQuizActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.organism_quiz_activity)

        val etName = findViewById<EditText>(R.id.etName)
        val btnStart = findViewById<Button>(R.id.btnStart)

        btnStart.setOnClickListener {
            if (etName.text.isEmpty()) {
                Toast.makeText(this, "Please, enter your name", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, OrganismQuizQuestion::class.java)
                intent.putExtra(Constants.USER_NAME, etName.text.toString())
                startActivity(intent)
//                finish()
            }
        }
    }
}