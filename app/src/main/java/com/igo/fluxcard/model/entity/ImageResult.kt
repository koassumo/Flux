package com.igo.fluxcard.model.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageResult(
    val total: Int,
    @Json(name = "total_pages") val totalPages: Int,
    val results: List<ImageDetails>
)

@JsonClass(generateAdapter = true)
data class ImageDetails(
    val id: String,
    val urls: ImageUrls,
    @Json(name = "alt_description") val altDescription: String?
)

@JsonClass(generateAdapter = true)
data class ImageUrls(
    val regular: String
)
