package com.dishant26201.wordquiz

import com.google.gson.annotations.SerializedName

data class QuizAndParams(
    @SerializedName("quizlist") val quizlist : List<Questions>,
)

data class Questions(
    @SerializedName("quiz") val quiz : List<String>,
    @SerializedName("option") val option : List<String>,
    @SerializedName("correct") val correct : Int,
)