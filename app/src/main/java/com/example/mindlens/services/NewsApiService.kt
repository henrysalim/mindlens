package com.example.mindlens.services

import com.example.mindlens.data.NewsApiResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API Service for fetching articles
interface NewsApiService {
    @GET("api/v4/search")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("lang") lang: String = "id",
        @Query("country") country: String = "id",
        @Query("sortby") sortBy: String = "publishedAt",
        @Query("page") page: Int,
        @Query("max") max: Int = 10,
        @Query("apikey") apiKey: String
    ): Response<NewsApiResponse>

    companion object {
        private const val BASE_URL = "https://gnews.io/" // gnews base URL

        // create Retrofit service for article API
        fun create(): NewsApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }
}