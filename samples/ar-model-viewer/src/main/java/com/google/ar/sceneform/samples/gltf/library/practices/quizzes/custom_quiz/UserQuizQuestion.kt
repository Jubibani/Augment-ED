package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.custom_quiz

data class UserQuizQuestion(
    val questionText: String,
    val choices: List<String>,
    val correctAnswerIndex: Int
)