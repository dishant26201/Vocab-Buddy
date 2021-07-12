package com.dishant26201.test

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


public interface ShortDefService {

    @GET("{word}?key=???")
    fun getShortDef(
        @Path("word") word: String
    ): Call<List<ShortDef>>
}
