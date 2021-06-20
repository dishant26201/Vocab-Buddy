package com.dishant26201.quizapp

data class Question(

    val id: Int, // question id
    val question: String, // hardcoded question text
    val word1: String, // question word 1
    val word2: String, // question word 2
    val word3: String, // question word 3
    val op1: String, // answer option 1
    val op2: String, // answer option 2
    val correctOp : Int, // correct answer position
)