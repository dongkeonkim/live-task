package com.example.kanban.controller

import com.example.kanban.dto.CreateTaskRequest
import com.example.kanban.dto.TaskDto
import com.example.kanban.dto.UpdateTaskRequest
import com.example.kanban.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "태스크", description = "칸반 보드 태스크 관리 API")
class TaskController(private val taskService: TaskService) {

    @Operation(summary = "태스크 목록 조회", description = "현재 사용자의 모든 태스크를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun getTasks(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<TaskDto>> =
        ResponseEntity.ok(taskService.getTasks(userDetails.username))

    @Operation(summary = "태스크 생성", description = "새로운 태스크를 생성합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "생성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청")
    )
    @PostMapping
    fun createTask(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: CreateTaskRequest
    ): ResponseEntity<TaskDto> =
        ResponseEntity.ok(taskService.createTask(userDetails.username, request))

    @Operation(summary = "태스크 수정", description = "기존 태스크를 수정합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "404", description = "태스크를 찾을 수 없음"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
    @PutMapping("/{id}")
    fun updateTask(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "태스크 ID") @PathVariable id: Long,
        @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<TaskDto> =
        ResponseEntity.ok(taskService.updateTask(userDetails.username, id, request))

    @Operation(summary = "태스크 삭제", description = "태스크를 삭제합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "삭제 성공"),
        ApiResponse(responseCode = "404", description = "태스크를 찾을 수 없음"),
        ApiResponse(responseCode = "403", description = "권한 없음")
    )
    @DeleteMapping("/{id}")
    fun deleteTask(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Parameter(description = "태스크 ID") @PathVariable id: Long
    ): ResponseEntity<Void> {
        taskService.deleteTask(userDetails.username, id)
        return ResponseEntity.ok().build()
    }
}
