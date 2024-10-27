package com.igo.fluxcard.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val origin: String,
    val translate: String,
    var correctStreak: Int = 0,    // Количество правильных ответов
    var lastShownTimestamp: Long = 0L, // Время последнего показа слова
    var showFirst: Int = 0,
    var showSecond: Int = 0,
    var showThird: Int = 0,
    var showFourth: Int = 0,
    var showFifth: Int = 0,
    var showFirstTimestamp: Long = 0L,
    var showSecondTimestamp: Long = 0L,
    var showThirdTimestamp: Long = 0L,
    var showFourthTimestamp: Long = 0L,
    var showFifthTimestamp: Long = 0L
)
