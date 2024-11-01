package com.igo.fluxcard.model.entity

import com.squareup.moshi.Json

data class Collection(
    val cardSets: List<CardSetInfo>
)

data class CardSetInfo(
    val id: Int,
    val urlName: String,
    val name: String,
    val description: String,
    @Json(name = "total_cards") val totalCards: Int,
    val status: String,
    val languages: String
)
