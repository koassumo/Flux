package com.igo.fluxcard.model.entity

data class CardSet(
    val id: Int,
    val name: String,
    val description: String,
    val cards: List<Card>
)
