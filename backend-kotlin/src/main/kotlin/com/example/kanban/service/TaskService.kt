package com.example.kanban.service

import com.example.kanban.dto.CreateTaskRequest
import com.example.kanban.dto.TaskDto
import com.example.kanban.dto.UpdateTaskRequest
import com.example.kanban.entity.Task
import com.example.kanban.entity.TaskStatus
import com.example.kanban.exception.TaskNotFoundException
import com.example.kanban.exception.UnauthorizedAccessException
import com.example.kanban.repository.TaskRepository
import com.example.kanban.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {

    fun getTasks(username: String): List<TaskDto> {
        val user = getUser(username)
        return taskRepository.findAllByUserOrderByOrderAsc(user)
            .map { mapToDto(it) }
    }

    fun createTask(username: String, request: CreateTaskRequest): TaskDto {
        val user = getUser(username)

        val task = Task(
            title = request.title,
            description = request.description,
            status = TaskStatus.TODO,
            order = System.currentTimeMillis(),
            user = user
        )

        val savedTask = taskRepository.save(task)
        return mapToDto(savedTask)
    }

    fun updateTask(username: String, taskId: Long, request: UpdateTaskRequest): TaskDto {
        val task = taskRepository.findById(taskId)
            .orElseThrow { TaskNotFoundException("태스크를 찾을 수 없습니다.") }

        if (task.user.username != username) {
            throw UnauthorizedAccessException("해당 태스크에 대한 권한이 없습니다.")
        }

        request.title?.let { task.title = it }
        request.description?.let { task.description = it }
        request.status?.let { task.status = it }
        request.order?.let { task.order = it }

        return mapToDto(taskRepository.save(task))
    }

    fun deleteTask(username: String, taskId: Long) {
        val task = taskRepository.findById(taskId)
            .orElseThrow { TaskNotFoundException("태스크를 찾을 수 없습니다.") }

        if (task.user.username != username) {
            throw UnauthorizedAccessException("해당 태스크에 대한 권한이 없습니다.")
        }

        taskRepository.delete(task)
    }

    private fun getUser(email: String) =
        userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다.")

    private fun mapToDto(task: Task) = TaskDto(
        id = task.id!!,
        title = task.title,
        description = task.description,
        status = task.status,
        order = task.order,
        creatorName = task.user.name,
        createdAt = task.createdAt
    )
}
