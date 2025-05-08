package com.google.ar.sceneform.samples.gltf.library.practices.quizzes.genetic_quiz

import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.practices.quizzes.organism_quiz.OrganismQuestions


object GeneticConstants {
    val USER_NAME: String = "user_name"
    val TOTAL_QUESTIONS: String = "total_questions"
    val SCORE: String = "score"

    fun getQuestions(): ArrayList<GeneticQuestions> {
        val questionsList = ArrayList<GeneticQuestions>()

        // 1
        val questionOne = GeneticQuestions(
            1,
            "What is the study of heredity and variation called?",
            R.drawable.genetics,
            arrayListOf("Cytology",
                "Genetics",
                "Ecology",
                "Physiology"
            ),
            1,
        )
        questionsList.add(questionOne)

        // 2
        val questionTwo = GeneticQuestions(
            2,
            "What are the genetic materials that serve as instructions for cell activities?",
            R.drawable.dna,
            arrayListOf(
                "Proteins",
                "Carbohydrates",
                "Lipids",
                "DNA"
            ),
            3
        )
        questionsList.add(questionTwo)

        // 3
        val questionThree = GeneticQuestions(
            3,
            "In eukaryotic cells, DNA is bound with proteins and organized into what structures?",
            R.drawable.chromosomes,
            arrayListOf("Ribosomes", "Mitochondria", "Chromosomes", "Nucleolus"),
            2
        )
        questionsList.add(questionThree)

        // 4
        val questionFour = GeneticQuestions(
            4,
            "How many chromosomes do humans normally have?",
            R.drawable.many_chromosomes,
            arrayListOf(
                "12",
                "24",
                "32",
                "46"
            ),
            1
        )
        questionsList.add(questionFour)

        // 5
        val questionFive = GeneticQuestions(
            5,
            "Which stage of the cell cycle is characterized by cell growth and DNA replication?",
            R.drawable.cell_cycle,
            arrayListOf(
                "M phase",
                "Interphase",
                "Prophase",
                "Telophase"
            ),
            1
        )
        questionsList.add(questionFive)

        // 6
        val questionSix = GeneticQuestions(
            6,
            "During which substage of interphase does DNA synthesis or replication occur?",
            R.drawable.mitosis_interphase,
            arrayListOf(
                "G1 phase",
                "S phase",
                "G2 phase",
                "M phase"
            ),
            1
        )

        questionsList.add(questionSix)

        // 7
        val questionSeven = GeneticQuestions(
            7,
            "What are the two types of cell division in eukaryotic cells?",
            R.drawable.mitosis_miosis,
            arrayListOf(
                "Fission and Budding",
                "Mitosis and Meiosis",
                "Photosynthesis and Respiration",
                "Transcription and Translation"
            ),
            1
        )
        questionsList.add(questionSeven)

        // 8
        val questionEight = GeneticQuestions(
            8,
            " Which type of cell division produces two identical daughter cells with the same number of chromosomes as the parent cell?",
            R.drawable.prophase,
            arrayListOf("Meiosis", "Mitosis", "Binary Fission", "Budding"),
            1
        )
        questionsList.add(questionEight)

        // 9
        val questionNine = GeneticQuestions(
            9,
            "What is the exchange of segments between sister chromatids of homologous chromosomes during Prophase I of meiosis called?",
            R.drawable.chromosome_x,
            arrayListOf("Synapsis", "Independent Assortment", "Crossing Over", "Fertilization"),
            2
        )
        questionsList.add(questionNine)

        // 10
        val questionTen = GeneticQuestions(
            10,
            "Which type of cell division reduces the number of chromosomes by half and takes place during the formation of gametes (sex cells)?",
            R.drawable.last_chromosomes,
            arrayListOf("Mitosis", "Meiosis", "Fertilization", "Differentiation"),
            1
        )
        questionsList.add(questionTen)


        return questionsList

    }
}