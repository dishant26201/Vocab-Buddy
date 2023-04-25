package com.dishant26201.wordquiz

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


public interface TwinwordService {

    @Headers(
        "X-RapidAPI-Key: a880f66bdbmshc67db687f112d5dp1d9d51jsne4fc5ee4b060",
        "X-RapidAPI-Host: twinword-word-association-quiz.p.rapidapi.com"
    )
    @GET("api/quiz/type1/latest/")
    fun getQuestions(
        @Query("level") level : String,
        @Query("area") area : String
    ) : Call<QuizAndParams>
}
