package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.custom_quiz

data class UserQuiz(
    val quizId: Long,
    val title: String,
    val questions: List<UserQuizQuestion>
)