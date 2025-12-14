package com.example.kanban.dto

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthenticationResponse(
    val token: String,
    val username: String
)
