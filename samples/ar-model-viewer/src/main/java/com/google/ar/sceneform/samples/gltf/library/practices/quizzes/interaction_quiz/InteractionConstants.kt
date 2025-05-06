package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.interaction_quiz

import com.google.ar.sceneform.samples.gltf.R



object InteractionConstants {
    val USER_NAME: String = "user_name"
    val TOTAL_QUESTIONS: String = "total_questions"
    val SCORE: String = "score"

    fun getQuestions(): ArrayList<InteractionQuestions> {
        val questionsList = ArrayList<InteractionQuestions>()

        // 1
        val questionOne = InteractionQuestions(
            1,
            "What is biomass?",
            R.drawable.foodchain,
            arrayListOf("The total amount of energy in an ecosystem",
                "The total mass of organisms in a food chain or a food web",
                "The rate at which energy is transferred between trophic levels",
                "The amount of sunlight captured by producers"
            ),
            1,
        )
        questionsList.add(questionOne)

        // 2
        val questionTwo = InteractionQuestions(
            2,
            "Besides producers and consumers, what third group of organisms is shown in some food webs and is responsible for breaking down dead organisms?",
            R.drawable.small_foodchain,
            arrayListOf("Herbivores", "Carnivores", "Omnivores", "Decomposers"),
            3
        )
        questionsList.add(questionTwo)

        // 3
        val questionThree = InteractionQuestions(
            3,
            "In a food web or food chain, what is the term for organisms that consume food for their energy supply?",
            R.drawable.third_foodchain,
            arrayListOf("Producers", "Decomposers", "Consumers", "Autotrophs"),
            2
        )
        questionsList.add(questionThree)

        // 4
        val questionFour = InteractionQuestions(
            4,
            "What happens to much of the energy that is not transferred to the next trophic level?",
            R.drawable.pyramid_energy,
            arrayListOf("It is stored as biomass", "It is lost as heat", "It is used for photosynthesis", "It is consumed by decomposers"),
            1
        )
        questionsList.add(questionFour)

        // 5
        val questionFive = InteractionQuestions(
            5,
            "In an energy pyramid, where is the largest amount of biomass and energy found?",
            R.drawable.cube_pyramid_energy,
            arrayListOf("At the top of the pyramid", "In the middle of the pyramid", "At the base of the pyramid", "It is equal at all levels"),
            2
        )
        questionsList.add(questionFive)

        // 6
        val questionSix = InteractionQuestions(
            6,
            "What is the most important factor that controls what kinds of organisms live in an ecosystem?",
            R.drawable.energy_flow,
            arrayListOf("The diversity of organisms", "The flow of energy", "The taxonomic classification", "The presence of decomposers"),
            1
        )

        questionsList.add(questionSix)

        // 7
        val questionSeven = InteractionQuestions(
            7,
            "The process by which plants, some bacteria, and algae convert light energy into chemical energy using water, carbon dioxide, and sunlight is called:",
            R.drawable.sunlight,
            arrayListOf("Respiration", "Decomposition", "Photosynthesis", "Consumption"),
            2
        )
        questionsList.add(questionSeven)

        // 8
        val questionEight = InteractionQuestions(
            8,
            "Food chains may be interconnected to form a more complex feeding relationship called a:",
            R.drawable.last_foodchain,
            arrayListOf("Energy pyramid", "Biomass pyramid", "Food web", "Trophic level"),
            2
        )
        questionsList.add(questionEight)


        return questionsList
    }
}