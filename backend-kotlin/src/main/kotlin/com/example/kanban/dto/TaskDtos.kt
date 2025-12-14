package com.example.kanban.dto

import com.example.kanban.entity.TaskStatus
import java.time.LocalDateTime

data class CreateTaskRequest(
    val title: String,
    val description: String? = null
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val order: Long? = null
)

data class TaskDto(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val order: Long?,
    val creatorName: String,
    val createdAt: LocalDateTime
)
