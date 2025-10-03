package com.example.gaurdiancircle.responses

data class GuardedLocationResponse(
    val status: String,
    val location: LocationData?,
    val message: String?
)

data class LocationData(
    val user_id: Int,
    val latitude: String,
    val longitude: String,
    val timestamp: String
)