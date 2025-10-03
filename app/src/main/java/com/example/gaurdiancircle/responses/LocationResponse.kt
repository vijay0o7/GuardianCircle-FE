package com.example.gaurdiancircle.responses

data class LocationResponse(
    val latitude: Double,
    val longitude: Double,
    val status: String? = null
)
