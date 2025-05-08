package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.nutrition_quiz

data class NutritionQuestions(
    val id: Int,
    val questionText: String,
    val image: Int,
    val alternatives: ArrayList<String>,
    val correctAnswerIndex: Int,
)
