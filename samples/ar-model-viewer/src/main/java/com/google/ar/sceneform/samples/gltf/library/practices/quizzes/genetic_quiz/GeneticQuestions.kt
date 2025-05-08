package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.genetic_quiz

data class GeneticQuestions(
    val id: Int,
    val questionText: String,
    val image: Int,
    val alternatives: ArrayList<String>,
    val correctAnswerIndex: Int,
)
