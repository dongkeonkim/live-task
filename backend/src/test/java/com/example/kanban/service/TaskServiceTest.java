package com.example.kanban.service;

import com.example.kanban.dto.CreateTaskRequest;
import com.example.kanban.dto.TaskDto;
import com.example.kanban.dto.UpdateTaskRequest;
import com.example.kanban.entity.Task;
import com.example.kanban.entity.TaskStatus;
import com.example.kanban.entity.User;
import com.example.kanban.exception.TaskNotFoundException;
import com.example.kanban.exception.UnauthorizedAccessException;
import com.example.kanban.repository.TaskRepository;
import com.example.kanban.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;
    private CreateTaskRequest createRequest;
    private UpdateTaskRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("테스트사용자")
                .email("test@example.com")
                .password("encodedPassword")
                .build();

        task = Task.builder()
                .id(1L)
                .title("테스트 태스크")
                .description("테스트 설명")
                .status(TaskStatus.TODO)
                .order(1000L)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateTaskRequest.builder()
                .title("새 태스크")
                .description("새 태스크 설명")
                .build();

        updateRequest = UpdateTaskRequest.builder()
                .title("수정된 태스크")
                .description("수정된 설명")
                .status(TaskStatus.IN_PROGRESS)
                .build();
    }

    @Test
    @DisplayName("태스크 목록 조회 성공")
    void getTasks_Success() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(taskRepository.findAllByUserOrderByOrderAsc(any(User.class)))
                .thenReturn(List.of(task));

        // when
        List<TaskDto> tasks = taskService.getTasks("test@example.com");

        // then
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("테스트 태스크");
    }

    @Test
    @DisplayName("태스크 생성 성공")
    void createTask_Success() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // when
        TaskDto result = taskService.createTask("test@example.com", createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 태스크");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("태스크 수정 성공")
    void updateTask_Success() {
        // given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // when
        TaskDto result = taskService.updateTask("test@example.com", 1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("태스크 수정 실패 - 태스크 없음")
    void updateTask_Fail_TaskNotFound() {
        // given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> taskService.updateTask("test@example.com", 1L, updateRequest))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("태스크 수정 실패 - 권한 없음")
    void updateTask_Fail_Unauthorized() {
        // given
        User otherUser = User.builder()
                .id(2L)
                .name("다른사용자")
                .email("other@example.com")
                .password("encodedPassword")
                .build();
        Task otherTask = Task.builder()
                .id(1L)
                .title("다른 태스크")
                .user(otherUser)
                .build();
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(otherTask));

        // when & then
        assertThatThrownBy(() -> taskService.updateTask("test@example.com", 1L, updateRequest))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("태스크 삭제 성공")
    void deleteTask_Success() {
        // given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(any(Task.class));

        // when
        taskService.deleteTask("test@example.com", 1L);

        // then
        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("태스크 삭제 실패 - 태스크 없음")
    void deleteTask_Fail_TaskNotFound() {
        // given
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> taskService.deleteTask("test@example.com", 1L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("태스크 삭제 실패 - 권한 없음")
    void deleteTask_Fail_Unauthorized() {
        // given
        User otherUser = User.builder()
                .id(2L)
                .name("다른사용자")
                .email("other@example.com")
                .password("encodedPassword")
                .build();
        Task otherTask = Task.builder()
                .id(1L)
                .title("다른 태스크")
                .user(otherUser)
                .build();
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(otherTask));

        // when & then
        assertThatThrownBy(() -> taskService.deleteTask("test@example.com", 1L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
