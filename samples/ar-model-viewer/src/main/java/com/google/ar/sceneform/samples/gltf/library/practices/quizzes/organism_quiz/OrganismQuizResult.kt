package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.organism_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.screens.MainActivity
import com.google.ar.sceneform.samples.gltf.library.screens.PracticeActivity

class OrganismQuizResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_activity_result)

        val userName = intent.getStringExtra(Constants.USER_NAME)
        val totalQuestions = intent.getIntExtra(Constants.TOTAL_QUESTIONS, 0)
        val score = intent.getIntExtra(Constants.SCORE, 0)

        val congratulationsTv: TextView = findViewById(R.id.congratulationsTv)
        val scoreTv: TextView = findViewById(R.id.scoreTv)
        val btnRestart: Button = findViewById(R.id.btnRestart)
        val btnExit: Button = findViewById(R.id.btnExit) // New Exit button

        congratulationsTv.text = "Congratulations"
        scoreTv.text = "Your score is $score of $totalQuestions"

        // Restart button logic: Redirect to the quiz activity
        btnRestart.setOnClickListener {
            val intent = Intent(this, OrganismQuizQuestion::class.java) // Restart the quiz
            startActivity(intent)
            finish()
        }

        btnExit.setOnClickListener {
            val intent = Intent(this, PracticeActivity::class.java) // Redirect to PracticeActivity
            startActivity(intent)
            finish()
        }
    }
}