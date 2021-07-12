package com.dishant26201.test

import com.google.gson.annotations.SerializedName

data class WordResults(
    @SerializedName("word") val word : String,
    @SerializedName("phonetics") val phonetics : List<Pronounciation>,
    @SerializedName("meanings") val meanings : List<Meaning>
)

data class Pronounciation(
    @SerializedName("audio") val audio : String
)

data class Meaning(
    @SerializedName("partOfSpeech") val partOfSpeech : String,
    @SerializedName("definitions") val definitions : List<Definition>
)

data class Definition(
    @SerializedName("definition") val definition : String,
    @SerializedName("example") val example : String,
    @SerializedName("synonyms") val synonyms : List<String>
)