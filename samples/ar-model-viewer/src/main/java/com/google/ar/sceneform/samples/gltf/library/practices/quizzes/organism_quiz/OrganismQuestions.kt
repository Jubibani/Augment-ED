package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.organism_quiz

data class OrganismQuestions(
    val id: Int,
    val questionText: String,
    val image: Int,
    val alternatives: ArrayList<String>,
    val correctAnswerIndex: Int,
)
