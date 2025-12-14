package com.example.kanban.repository

import com.example.kanban.entity.Task
import com.example.kanban.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<Task, Long> {
    fun findAllByUserOrderByOrderAsc(user: User): List<Task>
}
