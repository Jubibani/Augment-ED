package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.nutrition_quiz

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.screens.PracticeActivity

class NutritionQuizQuestion : AppCompatActivity() {
    private var userName: String? = null

    private val questionsList: ArrayList<NutritionQuestions> = NutritionConstants.getQuestions()
    private var currentQuestionIndex = 0;
    private var selectedAlternativeIndex = -1;
    private var isAnswerChecked = false;
    private var totalScore = 0;
    private val alternativesIds = arrayOf(R.id.optionOne, R.id.optionTwo, R.id.optionThree, R.id.optionFour)

    private var tvQuestion: TextView? = null
    private var ivImage: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var tvProgress: TextView? = null
    private var btnSubmit: Button? = null
    private var tvAlternatives: ArrayList<TextView>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.organism_quiz_activity_questions)

        userName = intent.getStringExtra(NutritionConstants.USER_NAME)

        tvQuestion = findViewById(R.id.tvQuestion)
        ivImage = findViewById(R.id.ivImage)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvAlternatives = arrayListOf(
            findViewById(R.id.optionOne),
            findViewById(R.id.optionTwo),
            findViewById(R.id.optionThree),
            findViewById(R.id.optionFour),
        )

        updateQuestion()

        // Close button functionality
        val closeButton: ImageButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            val intent = Intent(this, PracticeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSubmit?.setOnClickListener {
            if (!isAnswerChecked) {
                val anyAnswerIsChecked = selectedAlternativeIndex != -1
                if (!anyAnswerIsChecked) {
                    Toast.makeText(this, "Please, select an alternative", Toast.LENGTH_SHORT).show()
                } else {
                    val currentQuestion = questionsList[currentQuestionIndex]
                    if (
                        selectedAlternativeIndex == currentQuestion.correctAnswerIndex
                    ) {
                        answerView(tvAlternatives!![selectedAlternativeIndex], R.drawable.correct_option_border_bg)
                        totalScore++
                    } else {
                        answerView(tvAlternatives!![selectedAlternativeIndex], R.drawable.wrong_option_border_bg)
                        answerView(tvAlternatives!![currentQuestion.correctAnswerIndex], R.drawable.correct_option_border_bg)
                    }

                    isAnswerChecked = true
                    btnSubmit?.text = if (currentQuestionIndex == questionsList.size - 1) "FINISH" else "GO TO NEXT QUESTION"
                    selectedAlternativeIndex = -1
                }
            } else {
                if (currentQuestionIndex < questionsList.size - 1) {
                    currentQuestionIndex++
                    updateQuestion()
                } else {
                    val intent = Intent(this, NutritionQuizResult::class.java)
                    intent.putExtra(NutritionConstants.USER_NAME, userName)
                    intent.putExtra(NutritionConstants.TOTAL_QUESTIONS, questionsList.size)
                    intent.putExtra(NutritionConstants.SCORE, totalScore)
                    startActivity(intent)
                    finish()
                }

                isAnswerChecked = false
            }
        }

        tvAlternatives?.let {
            for (optionIndex in it.indices) {
                it[optionIndex].let {
                    it.setOnClickListener{
                        if (!isAnswerChecked) {
                            selectedAlternativeView(it as TextView, optionIndex)
                        }
                    }
                }
            }
        }
    }

    private fun updateQuestion() {
        defaultAlternativesView()

        // Render Question Text
        tvQuestion?.text = questionsList[currentQuestionIndex].questionText
        // Render Question Image
        ivImage?.setImageResource(questionsList[currentQuestionIndex].image)
        // progressBar
        progressBar?.progress = currentQuestionIndex + 1
        // Text of progress bar
        tvProgress?.text = "${currentQuestionIndex + 1}/${questionsList.size}"

        for (alternativeIndex in questionsList[currentQuestionIndex].alternatives.indices) {
            tvAlternatives!![alternativeIndex].text = questionsList[currentQuestionIndex].alternatives[alternativeIndex]
        }

        btnSubmit?.text = if (currentQuestionIndex == questionsList.size - 1) "FINISH" else "SUBMIT"
    }

    private fun defaultAlternativesView() {
        for (alternativeTv in tvAlternatives!!) {
            alternativeTv.typeface = Typeface.DEFAULT
            alternativeTv.setTextColor(Color.parseColor("#7A8089"))
            alternativeTv.background = ContextCompat.getDrawable(
                this@NutritionQuizQuestion,
                R.drawable.default_option_border_bg
            )
        }
    }

    private fun selectedAlternativeView(option: TextView, index: Int) {
        defaultAlternativesView()
        selectedAlternativeIndex = index

        option.setTextColor(
            Color.parseColor("#363A43")
        )
        option.setTypeface(option.typeface, Typeface.BOLD)
        option.background = ContextCompat.getDrawable(
            this@NutritionQuizQuestion,
            R.drawable.selected_option_border_bg
        )
    }

    private fun answerView(view: TextView, drawableId: Int) {
        view.background = ContextCompat.getDrawable(
            this@NutritionQuizQuestion,
            drawableId
        )
        tvAlternatives!![selectedAlternativeIndex].setTextColor(
            Color.parseColor("#FFFFFF")
        )
    }
}