package com.example.kanban.controller

import com.example.kanban.dto.AuthenticationResponse
import com.example.kanban.dto.LoginRequest
import com.example.kanban.dto.RegisterRequest
import com.example.kanban.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "사용자 인증 관련 API")
class AuthController(private val authService: AuthService) {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "회원가입 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청")
    )
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthenticationResponse> =
        ResponseEntity.ok(authService.register(request))

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    )
    @PostMapping("/login")
    fun authenticate(@RequestBody request: LoginRequest): ResponseEntity<AuthenticationResponse> =
        ResponseEntity.ok(authService.authenticate(request))
}
