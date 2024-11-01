package com.igo.fluxcard.model.entity

import com.squareup.moshi.Json

// Настройка Firebase 4 из 5.
data class Card(
    val id: Int,
    val title: String,
    val description: String
)


data class CardSet(
    val id: Int,
    val name: String,
    val description: String,
    @Json(name = "total_cards") val totalCards: Int,
    val status: String,
    val cards: List<Card>
)
