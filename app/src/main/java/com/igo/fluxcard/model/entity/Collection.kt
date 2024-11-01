package com.igo.fluxcard.model.entity

data class Collection(
    val cardSets: List<CardSetInfo>
)

data class CardSetInfo(
    val id: Int,
    val urlName: String,
    val name: String,
    val description: String,
    val totalCards: Int,
    val status: String,
    val languages: String
)
