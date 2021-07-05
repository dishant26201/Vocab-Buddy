package com.dishant26201.wordquiz

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query


public interface TwinwordService {

    @Headers("Demo")
    @GET("api/quiz/type1/latest/")
    fun getQuestions(
        @Query("level") level : String,
        @Query("area") area : String
    ) : Call<QuizAndParams>
}
