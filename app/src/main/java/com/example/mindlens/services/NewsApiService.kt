package com.example.mindlens.services

import com.example.mindlens.model.NewsApiResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("api/v4/search")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("lang") lang: String = "id", // Default bahasa Indonesia
        @Query("country") country: String = "id",
        @Query("sortby") sortBy: String = "publishedAt", // relevancy, publishedAt
        @Query("page") page: Int,
        @Query("max") max: Int = 10, // 10 artikel per halaman
        @Query("apikey") apiKey: String
    ): Response<NewsApiResponse>

    companion object {
        private const val BASE_URL = "https://gnews.io/"

        fun create(): NewsApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }
}