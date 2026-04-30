package com.chiko0085.testgym

data class Member(
    val id: String,
    var name: String,
    var username: String,
    var password: String,
    var remainingDays: Int,
    var lastInteraction: Long = System.currentTimeMillis(),
    var weight: Double = 0.0,
    var height: Double = 0.0
)
