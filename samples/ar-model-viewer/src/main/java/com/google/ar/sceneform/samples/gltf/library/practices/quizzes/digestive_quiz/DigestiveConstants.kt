package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.digestive_quiz

import com.google.ar.sceneform.samples.gltf.R



object DigestiveConstants {
    val USER_NAME: String = "user_name"
    val TOTAL_QUESTIONS: String = "total_questions"
    val SCORE: String = "score"

    fun getQuestions(): ArrayList<DigestiveQuestions> {
        val questionsList = ArrayList<DigestiveQuestions>()

        // 1
        val questionOne = DigestiveQuestions(
            1,
            "astric juices provide what kind of environment in the stomach?",
            R.drawable.ph_scale,
            arrayListOf("Alkaline",
                "Neutral",
                "Acidic",
                "Basic"
            ),
            2,
        )
        questionsList.add(questionOne)

        // 2
        val questionTwo = DigestiveQuestions(
            2,
            "How does digestion make nutrients available to the body?",
            R.drawable.digestive_system,
            arrayListOf(
                "By transporting undigested food",
                "By breaking down food into smaller parts",
                "By solidifying food in the stomach",
                "By removing water from food"
            ),
            1
        )
        questionsList.add(questionTwo)

        // 3
        val questionThree = DigestiveQuestions(
            3,
            "What starts to be produced in the stomach and some glands when you are hungry?",
            R.drawable.digestive_system_front,
            arrayListOf("Saliva", "Bile", "Gastric juices", "Intestinal juice"),
            3
        )
        questionsList.add(questionThree)

        // 4
        val questionFour = DigestiveQuestions(
            4,
            "What does the rumbling sound you sometimes hear in your stomach indicate?",
            R.drawable.body_part,
            arrayListOf(
                "Food is being digested rapidly",
                "The stomach is empty and gastric juices are being churned",
                "Water is being reabsorbed in the large intestine",
                "Nutrients are being absorbed in the small intestine"
            ),
            1
        )
        questionsList.add(questionFour)

        // 5
        val questionFive = DigestiveQuestions(
            5,
            "hrough what process do organisms obtain energy from the foods they eat?",
            R.drawable.nutrition,
            arrayListOf(
                "Respiration",
                "Excretion",
                "Digestion",
                "Circulation"
            ),
            2
        )
        questionsList.add(questionFive)

        // 6
        val questionSix = DigestiveQuestions(
            6,
            "Where are wastes temporarily stored before being excreted from the body?",
            R.drawable.stomach,
            arrayListOf(
                "Small intestine",
                "Stomach",
                "Rectum",
                "Colon"
            ),
            2
        )

        questionsList.add(questionSix)

        // 7
        val questionSeven = DigestiveQuestions(
            7,
            "What is a Complete Digestive System characterized by?",
            R.drawable.last_digestive_sysyem,
            arrayListOf(
                "One opening for both eating and waste removal",
                "A sac-like digestive cavity",
                "A tubelike digestive system with a mouth at one end and an anus at the other",
                "Digestion occurring only within individual cells"
            ),
            2
        )
        questionsList.add(questionSeven)

        // 8
        val questionEight = DigestiveQuestions(
            8,
            "Where does mechanical digestion primarily begin in humans?",
            R.drawable.drawing_digestive,
            arrayListOf("Stomach", "Small intestine", "Mouth", "Esophagus"),
            2
        )
        questionsList.add(questionEight)


        return questionsList
    }
}