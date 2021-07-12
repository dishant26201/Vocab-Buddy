package com.dishant26201.test

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


public interface ShortDefService {

    @GET("{word}?key=da32f93f-7157-4e81-800d-1e68b80297f7")
    fun getShortDef(
        @Path("word") word: String
    ): Call<List<ShortDef>>
}