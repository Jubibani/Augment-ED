package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.organism_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.repository.PointsRepository
import com.google.ar.sceneform.samples.gltf.library.screens.PracticeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrganismQuizResult : AppCompatActivity() {
    private lateinit var repository: PointsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_activity_result)

        val userName = intent.getStringExtra(Constants.USER_NAME)
        val totalQuestions = intent.getIntExtra(Constants.TOTAL_QUESTIONS, 0)
        val score = intent.getIntExtra(Constants.SCORE, 0)

        val congratulationsTv: TextView = findViewById(R.id.congratulationsTv)
        val scoreTv: TextView = findViewById(R.id.scoreTv)
        val btnRestart: Button = findViewById(R.id.btnRestart)
        val btnExit: Button = findViewById(R.id.btnExit)

        congratulationsTv.text = "Congratulations"
        scoreTv.text = "Your score is $score of $totalQuestions"

        // Initialize the PointsRepository
        val database = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
        repository = PointsRepository(database.brainPointsDao())

        // Update points based on the score
        updatePoints(score)

        // Restart button logic: Redirect to the quiz activity
        btnRestart.setOnClickListener {
            val intent = Intent(this, OrganismQuizQuestion::class.java)
            startActivity(intent)
            finish()
        }

        // Exit button logic: Redirect to PracticeActivity
        btnExit.setOnClickListener {
            val intent = Intent(this, PracticeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updatePoints(score: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentPoints = repository.getPoints()
            val newPoints = currentPoints + score * 10 // Earn 10 points per correct answer
            repository.updatePoints(newPoints)
        }
    }
}