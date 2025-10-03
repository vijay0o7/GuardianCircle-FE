package com.example.gaurdiancircle.models

data class Guardian(
    val id: Int,
    val userId: Int,
    val name: String,
    val email: String,
    val phone: String,
    val gender: String,
    val status: String
)
