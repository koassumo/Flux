package com.igo.fluxcard.model.repository

import com.igo.fluxcard.model.entity.ImageResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageApiService {
    @GET("search/photos")
    suspend fun getImage(
        @Query("query") query: String,
        @Query("client_id") clientId: String
    ): Response<ImageResult>
}
