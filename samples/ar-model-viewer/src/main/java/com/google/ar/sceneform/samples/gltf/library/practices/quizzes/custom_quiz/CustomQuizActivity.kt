package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.custom_quiz

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.samples.gltf.R

class CustomQuizActivity : AppCompatActivity() {
    // In-memory quiz store for this session
    private val questions = mutableListOf<UserQuizQuestion>()
    private var isAttempting = false
    private var currentAttemptIndex = 0
    private var score = 0

    // Views for creation
    private lateinit var layoutCreate: LinearLayout
    private lateinit var etQuizTitle: EditText
    private lateinit var etQuestion: EditText
    private lateinit var etChoice1: EditText
    private lateinit var etChoice2: EditText
    private lateinit var etChoice3: EditText
    private lateinit var etChoice4: EditText
    private lateinit var rgCorrect: RadioGroup
    private lateinit var btnAddQuestion: Button
    private lateinit var btnStartQuiz: Button

    // Views for attempt
    private lateinit var layoutAttempt: LinearLayout
    private lateinit var tvAttemptQuestion: TextView
    private lateinit var rgAttemptChoices: RadioGroup
    private lateinit var rbAttempt1: RadioButton
    private lateinit var rbAttempt2: RadioButton
    private lateinit var rbAttempt3: RadioButton
    private lateinit var rbAttempt4: RadioButton
    private lateinit var btnNextAttempt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_quiz)

        // Creation section
        layoutCreate = findViewById(R.id.layoutCreate)
        etQuizTitle = findViewById(R.id.etQuizTitle)
        etQuestion = findViewById(R.id.etQuestion)
        etChoice1 = findViewById(R.id.etChoice1)
        etChoice2 = findViewById(R.id.etChoice2)
        etChoice3 = findViewById(R.id.etChoice3)
        etChoice4 = findViewById(R.id.etChoice4)
        rgCorrect = findViewById(R.id.rgCorrect)
        btnAddQuestion = findViewById(R.id.btnAddQuestion)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)

        // Attempt section
        layoutAttempt = findViewById(R.id.layoutAttempt)
        tvAttemptQuestion = findViewById(R.id.tvAttemptQuestion)
        rgAttemptChoices = findViewById(R.id.rgAttemptChoices)
        rbAttempt1 = findViewById(R.id.rbAttempt1)
        rbAttempt2 = findViewById(R.id.rbAttempt2)
        rbAttempt3 = findViewById(R.id.rbAttempt3)
        rbAttempt4 = findViewById(R.id.rbAttempt4)
        btnNextAttempt = findViewById(R.id.btnNextAttempt)

        // Initially hide attempt section
        layoutAttempt.visibility = View.GONE

        btnAddQuestion.setOnClickListener {
            val questionText = etQuestion.text.toString()
            val choices = listOf(
                etChoice1.text.toString(),
                etChoice2.text.toString(),
                etChoice3.text.toString(),
                etChoice4.text.toString()
            )
            val correctIndex = when (rgCorrect.checkedRadioButtonId) {
                R.id.rb1Correct -> 0
                R.id.rb2Correct -> 1
                R.id.rb3Correct -> 2
                R.id.rb4Correct -> 3
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

        btnStartQuiz.setOnClickListener {
            if (etQuizTitle.text.isBlank() || questions.isEmpty()) {
                Toast.makeText(this, "Add a title and at least one question.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startAttemptQuiz()
        }

        btnNextAttempt.setOnClickListener {
            val selected = when (rgAttemptChoices.checkedRadioButtonId) {
                R.id.rbAttempt1 -> 0
                R.id.rbAttempt2 -> 1
                R.id.rbAttempt3 -> 2
                R.id.rbAttempt4 -> 3
                else -> -1
            }
            if (selected == -1) {
                Toast.makeText(this, "Please select an answer.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selected == questions[currentAttemptIndex].correctAnswerIndex) {
                score++
            }
            if (currentAttemptIndex < questions.size - 1) {
                currentAttemptIndex++
                showAttemptQuestion()
            } else {
                Toast.makeText(this, "Quiz finished! Score: $score/${questions.size}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun startAttemptQuiz() {
        isAttempting = true
        currentAttemptIndex = 0
        score = 0
        layoutCreate.visibility = View.GONE
        layoutAttempt.visibility = View.VISIBLE
        showAttemptQuestion()
    }

    private fun showAttemptQuestion() {
        rgAttemptChoices.clearCheck()
        val q = questions[currentAttemptIndex]
        tvAttemptQuestion.text = q.questionText
        val choices = q.choices
        rbAttempt1.text = choices.getOrNull(0) ?: ""
        rbAttempt2.text = choices.getOrNull(1) ?: ""
        rbAttempt3.text = choices.getOrNull(2) ?: ""
        rbAttempt4.text = choices.getOrNull(3) ?: ""
        rbAttempt1.visibility = if (choices.size > 0) View.VISIBLE else View.GONE
        rbAttempt2.visibility = if (choices.size > 1) View.VISIBLE else View.GONE
        rbAttempt3.visibility = if (choices.size > 2) View.VISIBLE else View.GONE
        rbAttempt4.visibility = if (choices.size > 3) View.VISIBLE else View.GONE
    }
}