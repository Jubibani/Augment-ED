package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.digestive_quiz

data class DigestiveQuestions(
    val id: Int,
    val questionText: String,
    val image: Int,
    val alternatives: ArrayList<String>,
    val correctAnswerIndex: Int,
)
