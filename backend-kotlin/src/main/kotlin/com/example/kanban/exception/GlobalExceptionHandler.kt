package com.example.kanban.exception

import com.example.kanban.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(e: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "충돌",
                message = e.message ?: "이미 존재하는 이메일입니다."
            ))

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFound(e: TaskNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "찾을 수 없음",
                message = e.message ?: "작업을 찾을 수 없습니다."
            ))

    @ExceptionHandler(UnauthorizedAccessException::class)
    fun handleUnauthorizedAccess(e: UnauthorizedAccessException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                error = "접근 거부",
                message = e.message ?: "접근 권한이 없습니다."
            ))

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFound(e: UsernameNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "찾을 수 없음",
                message = e.message ?: "사용자를 찾을 수 없습니다."
            ))

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(e: BadCredentialsException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "인증 실패",
                message = "이메일 또는 비밀번호가 올바르지 않습니다."
            ))
}
