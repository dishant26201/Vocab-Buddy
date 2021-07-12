package com.dishant26201.wordquiz

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


public interface TwinwordService {

    @Headers("X-Twaip-Key: ejxv9wESxNQ5XFJpS0KQpfUd36rzGcQXEmWfq0QQ+OmxiZ3S8LY1wSYY4AH5EqY9UWx2KxAgRNbZ9N6SUBsNqw==")
    @GET("api/quiz/type1/latest/")
    fun getQuestions(
        @Query("level") level : String,
        @Query("area") area : String
    ) : Call<QuizAndParams>
}
