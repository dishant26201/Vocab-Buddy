package com.dishant26201.test

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


public interface DictionaryService {

    @GET("entries/en_US/{word}")
    fun getMeaning(
        @Path("word") word: String
    ): Call<List<WordResults>>
}