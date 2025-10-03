package com.example.gaurdiancircle.data

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String
)

data class EmergencyContactListResponse(
    val status: String,
    val contacts: List<EmergencyContact>
)
