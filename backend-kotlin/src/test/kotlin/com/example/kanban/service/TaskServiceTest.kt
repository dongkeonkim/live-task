package com.example.kanban.service

import com.example.kanban.dto.CreateTaskRequest
import com.example.kanban.dto.UpdateTaskRequest
import com.example.kanban.entity.Task
import com.example.kanban.entity.TaskStatus
import com.example.kanban.entity.User
import com.example.kanban.exception.TaskNotFoundException
import com.example.kanban.exception.UnauthorizedAccessException
import com.example.kanban.repository.TaskRepository
import com.example.kanban.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.*

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock
    lateinit var taskRepository: TaskRepository

    @Mock
    lateinit var userRepository: UserRepository

    @InjectMocks
    lateinit var taskService: TaskService

    private lateinit var testUser: User
    private lateinit var testTask: Task

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = 1L,
            name = "테스트사용자",
            email = "test@example.com",
            password = "encodedPassword"
        )

        testTask = Task(
            id = 1L,
            title = "테스트 태스크",
            description = "테스트 설명",
            status = TaskStatus.TODO,
            order = 1000L,
            user = testUser
        )
    }

    @Test
    @DisplayName("태스크 목록 조회 성공")
    fun getTasks_Success() {
        // given
        whenever(userRepository.findByEmail("test@example.com")).thenReturn(testUser)
        whenever(taskRepository.findAllByUserOrderByOrderAsc(testUser)).thenReturn(listOf(testTask))

        // when
        val tasks = taskService.getTasks("test@example.com")

        // then
        assertEquals(1, tasks.size)
        assertEquals("테스트 태스크", tasks[0].title)
    }

    @Test
    @DisplayName("태스크 생성 성공")
    fun createTask_Success() {
        // given
        val request = CreateTaskRequest(
            title = "새 태스크",
            description = "새 태스크 설명"
        )

        whenever(userRepository.findByEmail("test@example.com")).thenReturn(testUser)
        whenever(taskRepository.save(any<Task>())).thenAnswer {
            val task = it.arguments[0] as Task
            Task(
                id = 1L,
                title = task.title,
                description = task.description,
                status = task.status,
                order = task.order,
                user = task.user
            )
        }

        // when
        val result = taskService.createTask("test@example.com", request)

        // then
        assertEquals("새 태스크", result.title)
        assertEquals(TaskStatus.TODO, result.status)
    }

    @Test
    @DisplayName("태스크 수정 성공")
    fun updateTask_Success() {
        // given
        val request = UpdateTaskRequest(
            title = "수정된 태스크",
            status = TaskStatus.IN_PROGRESS
        )

        whenever(taskRepository.findById(1L)).thenReturn(Optional.of(testTask))
        whenever(taskRepository.save(any<Task>())).thenAnswer { it.arguments[0] as Task }

        // when
        val result = taskService.updateTask("test@example.com", 1L, request)

        // then
        assertEquals("수정된 태스크", result.title)
        assertEquals(TaskStatus.IN_PROGRESS, result.status)
    }

    @Test
    @DisplayName("태스크 수정 실패 - 태스크 없음")
    fun updateTask_Fail_NotFound() {
        // given
        val request = UpdateTaskRequest(title = "수정된 태스크")

        whenever(taskRepository.findById(999L)).thenReturn(Optional.empty())

        // when & then
        assertThrows(TaskNotFoundException::class.java) {
            taskService.updateTask("test@example.com", 999L, request)
        }
    }

    @Test
    @DisplayName("태스크 수정 실패 - 권한 없음")
    fun updateTask_Fail_Unauthorized() {
        // given
        val request = UpdateTaskRequest(title = "수정된 태스크")

        whenever(taskRepository.findById(1L)).thenReturn(Optional.of(testTask))

        // when & then
        assertThrows(UnauthorizedAccessException::class.java) {
            taskService.updateTask("other@example.com", 1L, request)
        }
    }

    @Test
    @DisplayName("태스크 삭제 성공")
    fun deleteTask_Success() {
        // given
        whenever(taskRepository.findById(1L)).thenReturn(Optional.of(testTask))

        // when
        taskService.deleteTask("test@example.com", 1L)

        // then
        verify(taskRepository).delete(testTask)
    }

    @Test
    @DisplayName("태스크 삭제 실패 - 태스크 없음")
    fun deleteTask_Fail_NotFound() {
        // given
        whenever(taskRepository.findById(999L)).thenReturn(Optional.empty())

        // when & then
        assertThrows(TaskNotFoundException::class.java) {
            taskService.deleteTask("test@example.com", 999L)
        }
    }

    @Test
    @DisplayName("태스크 삭제 실패 - 권한 없음")
    fun deleteTask_Fail_Unauthorized() {
        // given
        whenever(taskRepository.findById(1L)).thenReturn(Optional.of(testTask))

        // when & then
        assertThrows(UnauthorizedAccessException::class.java) {
            taskService.deleteTask("other@example.com", 1L)
        }
    }
}
