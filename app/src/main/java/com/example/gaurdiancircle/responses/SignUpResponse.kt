package com.example.gaurdiancircle.responses

data class SignUpRequest (
    val name: String,
    val email: String,
    val phone_number: String,
    val password: String,
    val role: String             // Added role field for USER/GUARDIAN
)

data class SignUpResponse (
    val status: String,
    val message: String,
    val user: User?              // Made nullable to handle error responses without user
)

data class User(
    val s_no: Int,
    val name: String,
    val email: String,
    val phone: String
)
