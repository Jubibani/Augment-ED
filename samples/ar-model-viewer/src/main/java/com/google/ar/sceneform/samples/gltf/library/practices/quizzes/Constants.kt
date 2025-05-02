package com.google.ar.sceneform.samples.gltf.library.practices.quizzes

import com.google.ar.sceneform.samples.gltf.R


object Constants {
    val USER_NAME: String = "user_name"
    val TOTAL_QUESTIONS: String = "total_questions"
    val SCORE: String = "score"

    fun getQuestions(): ArrayList<OrganismQuestions> {
        val questionsList = ArrayList<OrganismQuestions>()

        // 1
        val questionOne = OrganismQuestions(
            1,
            "What country does this flag belongs to?",
            R.drawable.ic_flag_of_argentina,
            arrayListOf("Argentina", "Australia", "Armenia", "Austria"),
            0,
        )
        questionsList.add(questionOne)

        // 2
        val questionTwo = OrganismQuestions(
            2,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_australia,
            arrayListOf("Angola", "Austria",
                "Australia", "Armenia"),
            2
        )
        questionsList.add(questionTwo)

        // 3
        val questionThree = OrganismQuestions(
            3,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_brazil,
            arrayListOf("Belarus", "Belize",
                "Brunei", "Brazil"),
            3
        )
        questionsList.add(questionThree)

        // 4
        val questionFour = OrganismQuestions(
            4,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_belgium,
            arrayListOf("Bahamas", "Belgium",
                "Barbados", "Belize"),
            1
        )
        questionsList.add(questionFour)

        // 5
        val questionFive = OrganismQuestions(
            5,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_fiji,
            arrayListOf("Gabon", "France",
                "Fiji", "Finland"),
            2
        )
        questionsList.add(questionFive)

        // 6
        val questionSix = OrganismQuestions(
            6,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_germany,
            arrayListOf("Germany", "Georgia",
                "Greece", "none of these"),
            0
        )

        questionsList.add(questionSix)

        // 7
        val questionSeven = OrganismQuestions(
            7,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_denmark,
            arrayListOf("Dominica", "Egypt",
                "Denmark", "Ethiopia"),
            2
        )
        questionsList.add(questionSeven)

        // 8
        val questionEight = OrganismQuestions(
            8,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_india,
            arrayListOf("Ireland", "Iran",
                "Hungary", "India"),
            3
        )
        questionsList.add(questionEight)

        // 9
        val questionNine = OrganismQuestions(
            9,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_new_zealand,
            arrayListOf("Australia", "New Zealand",
                "Tuvalu", "United States of America"),
            1
        )
        questionsList.add(questionNine)

        // 10
        val questionTen = OrganismQuestions(
            10,
            "What country does this flag belong to?",
            R.drawable.ic_flag_of_kuwait,
            arrayListOf("Kuwait", "Jordan",
                "Sudan", "Palestine"),
            0
        )
        questionsList.add(questionTen)

        return questionsList
    }
}