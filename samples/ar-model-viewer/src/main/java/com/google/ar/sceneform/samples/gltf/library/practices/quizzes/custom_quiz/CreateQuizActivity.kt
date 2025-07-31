package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.custom_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.samples.gltf.R

object InMemoryQuizRepo {
    var quizzes = mutableListOf<UserQuiz>()
    var nextId = 1L
}

class CreateQuizActivity : AppCompatActivity() {
    private val questions = mutableListOf<UserQuizQuestion>()
    private lateinit var etQuizTitle: EditText
    private lateinit var etQuestion: EditText
    private lateinit var etChoice1: EditText
    private lateinit var etChoice2: EditText
    private lateinit var etChoice3: EditText
    private lateinit var etChoice4: EditText
    private lateinit var rgCorrect: RadioGroup
    private lateinit var btnAddQuestion: Button
    private lateinit var btnSaveQuiz: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        etQuizTitle = findViewById(R.id.etQuizTitle)
        etQuestion = findViewById(R.id.etQuestion)
        etChoice1 = findViewById(R.id.etChoice1)
        etChoice2 = findViewById(R.id.etChoice2)
        etChoice3 = findViewById(R.id.etChoice3)
        etChoice4 = findViewById(R.id.etChoice4)
        rgCorrect = findViewById(R.id.rgCorrect)
        btnAddQuestion = findViewById(R.id.btnAddQuestion)
        btnSaveQuiz = findViewById(R.id.btnSaveQuiz)

        btnAddQuestion.setOnClickListener {
            val questionText = etQuestion.text.toString()
            val choices = listOf(
                etChoice1.text.toString(),
                etChoice2.text.toString(),
                etChoice3.text.toString(),
                etChoice4.text.toString()
            )
            val correctIndex = when (rgCorrect.checkedRadioButtonId) {
                R.id.rb1 -> 0
                R.id.rb2 -> 1
                R.id.rb3 -> 2
                R.id.rb4 -> 3
                else -> -1
            }
            if (questionText.isBlank() || choices.any { it.isBlank() } || correctIndex == -1) {
                Toast.makeText(this, "Complete all fields and select the correct answer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            questions.add(
                UserQuizQuestion(
                    questionText = questionText,
                    choices = choices,
                    correctAnswerIndex = correctIndex
                )
            )
            etQuestion.text.clear()
            etChoice1.text.clear()
            etChoice2.text.clear()
            etChoice3.text.clear()
            etChoice4.text.clear()
            rgCorrect.clearCheck()
            Toast.makeText(this, "Question added.", Toast.LENGTH_SHORT).show()
        }

        btnSaveQuiz.setOnClickListener {
            val title = etQuizTitle.text.toString()
            if (title.isBlank() || questions.isEmpty()) {
                Toast.makeText(this, "Add a title and at least one question.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            InMemoryQuizRepo.quizzes.add(
                UserQuiz(
                    quizId = InMemoryQuizRepo.nextId++,
                    title = title,
                    questions = questions.toList()
                )
            )
            Toast.makeText(this, "Quiz saved!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, UserQuizListActivity::class.java))
            finish()
        }
    }
}