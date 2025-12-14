package com.example.kanban.service

import com.example.kanban.dto.AuthenticationResponse
import com.example.kanban.dto.LoginRequest
import com.example.kanban.dto.RegisterRequest
import com.example.kanban.entity.User
import com.example.kanban.exception.EmailAlreadyExistsException
import com.example.kanban.repository.UserRepository
import com.example.kanban.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {

    fun register(request: RegisterRequest): AuthenticationResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException("이미 존재하는 이메일입니다.")
        }

        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        userRepository.save(user)

        val jwtToken = jwtTokenProvider.generateToken(user)
        return AuthenticationResponse(
            token = jwtToken,
            username = user.name
        )
    }

    fun authenticate(request: LoginRequest): AuthenticationResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = userRepository.findByEmail(request.email)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다.")

        val jwtToken = jwtTokenProvider.generateToken(user)
        return AuthenticationResponse(
            token = jwtToken,
            username = user.name
        )
    }
}
