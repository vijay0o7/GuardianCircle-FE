package com.example.gaurdiancircle.responses

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String,
    val name: String?,
    val phone_number: String?,       // nullable, might be null on error
    val email: String?,      // nullable, might be null on error
    val role: String?,       // nullable
    val userId: Int?        // <-- Added this so we can store & use it
)
