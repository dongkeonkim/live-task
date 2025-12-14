package com.example.kanban.service

import com.example.kanban.dto.LoginRequest
import com.example.kanban.dto.RegisterRequest
import com.example.kanban.entity.User
import com.example.kanban.exception.EmailAlreadyExistsException
import com.example.kanban.repository.UserRepository
import com.example.kanban.security.JwtTokenProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var passwordEncoder: PasswordEncoder

    @Mock
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Mock
    lateinit var authenticationManager: AuthenticationManager

    @InjectMocks
    lateinit var authService: AuthService

    @Test
    @DisplayName("회원가입 성공")
    fun register_Success() {
        // given
        val request = RegisterRequest(
            name = "테스트사용자",
            email = "test@example.com",
            password = "password123"
        )

        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] as User }
        whenever(jwtTokenProvider.generateToken(any<User>())).thenReturn("jwt-token")

        // when
        val response = authService.register(request)

        // then
        assertEquals("jwt-token", response.token)
        assertEquals("테스트사용자", response.username)
        verify(userRepository).save(any<User>())
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    fun register_Fail_EmailExists() {
        // given
        val request = RegisterRequest(
            name = "테스트사용자",
            email = "test@example.com",
            password = "password123"
        )

        whenever(userRepository.existsByEmail(request.email)).thenReturn(true)

        // when & then
        assertThrows(EmailAlreadyExistsException::class.java) {
            authService.register(request)
        }
    }

    @Test
    @DisplayName("로그인 성공")
    fun authenticate_Success() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        val user = User(
            id = 1L,
            name = "테스트사용자",
            email = "test@example.com",
            password = "encodedPassword"
        )

        whenever(authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()))
            .thenReturn(mock())
        whenever(userRepository.findByEmail(request.email)).thenReturn(user)
        whenever(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token")

        // when
        val response = authService.authenticate(request)

        // then
        assertEquals("jwt-token", response.token)
        assertEquals("테스트사용자", response.username)
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    fun authenticate_Fail_UserNotFound() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        whenever(authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()))
            .thenReturn(mock())
        whenever(userRepository.findByEmail(request.email)).thenReturn(null)

        // when & then
        assertThrows(NoSuchElementException::class.java) {
            authService.authenticate(request)
        }
    }
}
