package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.custom_quiz

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.samples.gltf.R

class UserQuizAttemptActivity : AppCompatActivity() {
    private var questions: List<UserQuizQuestion> = listOf()
    private var currentIndex = 0
    private var score = 0

    private lateinit var tvQuestion: TextView
    private lateinit var rbChoices: List<RadioButton>
    private lateinit var rgChoices: RadioGroup
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_quiz)

        val quizIndex = intent.getIntExtra("QUIZ_INDEX", -1)
        val quiz = InMemoryQuizRepo.quizzes.getOrNull(quizIndex)
        if (quiz == null) {
            Toast.makeText(this, "Quiz not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        questions = quiz.questions

        tvQuestion = findViewById(R.id.tvQuestion)
        rgChoices = findViewById(R.id.rgChoices)
        rbChoices = listOf(
            findViewById(R.id.rbChoice1),
            findViewById(R.id.rbChoice2),
            findViewById(R.id.rbChoice3),
            findViewById(R.id.rbChoice4)
        )
        btnNext = findViewById(R.id.btnNext)

        showQuestion()

        btnNext.setOnClickListener {
            val selected = when (rgChoices.checkedRadioButtonId) {
                R.id.rbChoice1 -> 0
                R.id.rbChoice2 -> 1
                R.id.rbChoice3 -> 2
                R.id.rbChoice4 -> 3
                else -> -1
            }
            if (selected == -1) {
                Toast.makeText(this, "Please select an answer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selected == questions[currentIndex].correctAnswerIndex) {
                score++
            }
            if (currentIndex < questions.size - 1) {
                currentIndex++
                showQuestion()
            } else {
                Toast.makeText(this, "Quiz finished! Score: $score/${questions.size}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showQuestion() {
        rgChoices.clearCheck()
        val q = questions[currentIndex]
        tvQuestion.text = q.questionText
        for (i in rbChoices.indices) {
            rbChoices[i].text = q.choices.getOrNull(i) ?: ""
            rbChoices[i].isEnabled = q.choices.getOrNull(i) != null && q.choices.getOrNull(i)!!.isNotBlank()
            rbChoices[i].visibility = if (q.choices.getOrNull(i).isNullOrBlank()) {
                RadioButton.GONE
            } else {
                RadioButton.VISIBLE
            }
        }
    }
}