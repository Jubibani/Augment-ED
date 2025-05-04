package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.organism_quiz

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
            "Which of the following is the largest category in the classification of organisms?",
            R.drawable.living_things_diagram,
            arrayListOf("Species", "Family", "Kingdom", "Domain"),
            3,
        )
        questionsList.add(questionOne)

        // 2
        val questionTwo = OrganismQuestions(
            2,
            "Which of the following is the smallest category in the classification of organisms?",
            R.drawable.living_things_diagram,
            arrayListOf("Family", "Genus", "Species", "Class"),
            2
        )
        questionsList.add(questionTwo)

        // 3
        val questionThree = OrganismQuestions(
            3,
            "How many domains are organisms currently classified into?",
            R.drawable.three_domains,
            arrayListOf("3", "2", "6", "5"),
            0
        )
        questionsList.add(questionThree)

        // 4
        val questionFour = OrganismQuestions(
            4,
            "Which of the following is NOT included in the Domain Eukarya?",
            R.drawable.three_domains_four_kingdom,
            arrayListOf("Protists", "Fungi", "Archaea", "Animals"),
            2
        )
        questionsList.add(questionFour)

        // 5
        val questionFive = OrganismQuestions(
            5,
            "What are Eubacteria commonly called?",
            R.drawable.eubacteria,
            arrayListOf("Archaea", "Protists", "True Bacteria", "Fungi"),
            2
        )
        questionsList.add(questionFive)

        // 6
        val questionSix = OrganismQuestions(
            6,
            "Into which two domains have prokaryotes been recently divided?",
            R.drawable.three_domains_bacteria,
            arrayListOf("Bacteria and Fungi", "Archaea and Protista", "Eukarya and Bacteria", "Archaea and Bacteria"),
            3
        )

        questionsList.add(questionSix)

        // 7
        val questionSeven = OrganismQuestions(
            7,
            "Escherichia coli (E. coli) naturally lives in the large intestine of humans and provides what essential substance?",
            R.drawable.human_diagram,
            arrayListOf("Vitamin C", "Lactic Acid", "Vitamin B12", "Methane"),
            2
        )
        questionsList.add(questionSeven)

        // 8
        val questionEight = OrganismQuestions(
            8,
            "Fungi have cell walls made up of what substance?",
            R.drawable.fungi,
            arrayListOf("Cellulose", "Peptidoglycan", "Chitin", "Silica"),
            2
        )
        questionsList.add(questionEight)

        // 9
        val questionNine = OrganismQuestions(
            9,
            "In bread mold Rhizopus, where are spores produced during asexual reproduction?",
            R.drawable.sporangium,
            arrayListOf("n the sporangium ", "In the hyphae", "In the basidium", "In the ascus"),
            0
        )
        questionsList.add(questionNine)

        // 10
        val questionTen = OrganismQuestions(
            10,
            "Which heterotroph moves using cilia attached to parts or all over its body?",
            R.drawable.paramecium,
            arrayListOf("Amoeba", "Euglena", "Trypanosoma", "Paramecium"),
            3
        )
        questionsList.add(questionTen)

        return questionsList
    }
}