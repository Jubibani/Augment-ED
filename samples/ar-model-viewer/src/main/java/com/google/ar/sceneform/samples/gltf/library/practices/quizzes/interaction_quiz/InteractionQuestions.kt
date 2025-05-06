package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.interaction_quiz

data class InteractionQuestions(
    val id: Int,
    val questionText: String,
    val image: Int,
    val alternatives: ArrayList<String>,
    val correctAnswerIndex: Int,
)
