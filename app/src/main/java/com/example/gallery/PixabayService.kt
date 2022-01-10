package com.example.gallery

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayService {
    @GET("?key=21629751-645aaa88c718127fa1b066aba&per_page=50")
    suspend fun searchPhoto(@Query("q") query: String, @Query("page") page: Int): Pixabay

    companion object {
        private const val BASE_URL = "https://pixabay.com/api/"

        fun create(): PixabayService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PixabayService::class.java)
        }
    }
}