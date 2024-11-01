package com.igo.fluxcard.model.entity

// Настройка Firebase 4.2 из 5.
data class CardSet(
    val id: Int,
    val name: String,
    val description: String,
    val total_cards: Int,
    val status: String,
    val cards: List<Card>
)

