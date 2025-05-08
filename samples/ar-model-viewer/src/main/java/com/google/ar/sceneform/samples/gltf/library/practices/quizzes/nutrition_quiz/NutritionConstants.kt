package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.nutrition_quiz

import com.google.ar.sceneform.samples.gltf.R



object NutritionConstants {
    val USER_NAME: String = "user_name"
    val TOTAL_QUESTIONS: String = "total_questions"
    val SCORE: String = "score"

    fun getQuestions(): ArrayList<NutritionQuestions> {
        val questionsList = ArrayList<NutritionQuestions>()

        // 1
        val questionOne = NutritionQuestions(
            1,
            "According to the Food and Nutrition Research Institute (FNRI) in the Philippines, which food guide is recommended for Filipinos to follow daily?",
            R.drawable.nutrition_pyramid,
            arrayListOf("Food Circle",
                "Food Square",
                "Food Pyramid",
                "Food Rectangle"
            ),
            2,
        )
        questionsList.add(questionOne)

        // 2
        val questionTwo = NutritionQuestions(
            2,
            "How many servings from the rice and rice products group should you eat each day according to the recommended daily servings for teenagers?",
            R.drawable.rice_servings,
            arrayListOf("3 servings", "6-8 servings", "1/2 servings", "1 glass"),
            1
        )
        questionsList.add(questionTwo)

        // 3
        val questionThree = NutritionQuestions(
            3,
            "Fiber is an important part of the diet because it helps with which bodily process?",
            R.drawable.vegetable_servings,
            arrayListOf("Providing energy", "Building complex molecules", "Regulating body temperature", "Cleaning the digestive tract"),
            3
        )
        questionsList.add(questionThree)

        // 4
        val questionFour = NutritionQuestions(
            4,
            "Approximately what percentage of your body is made up of water?",
            R.drawable.water_in_human_body,
            arrayListOf("30-40 percent", "55-60 percent", "70-80 percent", "90-95 percent"),
            1
        )
        questionsList.add(questionFour)

        // 5
        val questionFive = NutritionQuestions(
            5,
            "Insufficient intake of nutrients can lead to health problems, particularly in children, and may hinder what?",
            R.drawable.little_humans,
            arrayListOf("Exercise performance", "Sleep patterns", "Mental focus", "Growth and development"),
            3
        )
        questionsList.add(questionFive)

        // 6
        val questionSix = NutritionQuestions(
            6,
            "What is a potential consequence of not eating enough fiber?",
            R.drawable.energy_flow,
            arrayListOf("Increased energy levels", "Improved vision", "Stronger bones", "Constipation and other intestinal problems"),
            3
        )

        questionsList.add(questionSix)

        // 7
        val questionSeven = NutritionQuestions(
            7,
            "Drinking about 8 glasses of water a day is recommended to replace the amount of water lost through which bodily processes?",
            R.drawable.cup_of_water,
            arrayListOf(
                "Thinking and speaking",
                "Sweating, urinating, and respiration",
                "Sleeping and resting",
                "Eating and digesting"
            ),
            2
        )
        questionsList.add(questionSeven)

        return questionsList
    }
}