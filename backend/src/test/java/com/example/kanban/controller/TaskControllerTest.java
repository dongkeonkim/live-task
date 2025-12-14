package com.example.kanban.controller;

import com.example.kanban.dto.CreateTaskRequest;
import com.example.kanban.dto.TaskDto;
import com.example.kanban.dto.UpdateTaskRequest;
import com.example.kanban.entity.TaskStatus;
import com.example.kanban.exception.TaskNotFoundException;
import com.example.kanban.exception.UnauthorizedAccessException;
import com.example.kanban.security.JwtAuthenticationFilter;
import com.example.kanban.security.JwtTokenProvider;
import com.example.kanban.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
class TaskControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private TaskService taskService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        private TaskDto createTaskDto() {
                return TaskDto.builder()
                                .id(1L)
                                .title("테스트 태스크")
                                .description("테스트 설명")
                                .status(TaskStatus.TODO)
                                .order(1000L)
                                .creatorName("테스트사용자")
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        @Test
        @DisplayName("태스크 목록 조회 API 성공")
        @WithMockUser(username = "test@example.com")
        void getTasks_Success() throws Exception {
                // given
                when(taskService.getTasks(anyString())).thenReturn(List.of(createTaskDto()));

                // when & then
                mockMvc.perform(get("/api/tasks"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].title").value("테스트 태스크"));
        }

        @Test
        @DisplayName("태스크 생성 API 성공")
        @WithMockUser(username = "test@example.com")
        void createTask_Success() throws Exception {
                // given
                CreateTaskRequest request = CreateTaskRequest.builder()
                                .title("새 태스크")
                                .description("새 태스크 설명")
                                .build();

                when(taskService.createTask(anyString(), any(CreateTaskRequest.class)))
                                .thenReturn(createTaskDto());

                // when & then
                mockMvc.perform(post("/api/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("테스트 태스크"));
        }

        @Test
        @DisplayName("태스크 수정 API 성공")
        @WithMockUser(username = "test@example.com")
        void updateTask_Success() throws Exception {
                // given
                UpdateTaskRequest request = UpdateTaskRequest.builder()
                                .title("수정된 태스크")
                                .status(TaskStatus.IN_PROGRESS)
                                .build();

                TaskDto updatedTask = TaskDto.builder()
                                .id(1L)
                                .title("수정된 태스크")
                                .status(TaskStatus.IN_PROGRESS)
                                .order(1000L)
                                .creatorName("테스트사용자")
                                .createdAt(LocalDateTime.now())
                                .build();

                when(taskService.updateTask(anyString(), anyLong(), any(UpdateTaskRequest.class)))
                                .thenReturn(updatedTask);

                // when & then
                mockMvc.perform(put("/api/tasks/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("수정된 태스크"))
                                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("태스크 수정 API 실패 - 태스크 없음")
        @WithMockUser(username = "test@example.com")
        void updateTask_Fail_NotFound() throws Exception {
                // given
                UpdateTaskRequest request = UpdateTaskRequest.builder()
                                .title("수정된 태스크")
                                .build();

                when(taskService.updateTask(anyString(), anyLong(), any(UpdateTaskRequest.class)))
                                .thenThrow(new TaskNotFoundException("태스크를 찾을 수 없습니다."));

                // when & then
                mockMvc.perform(put("/api/tasks/999")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("태스크 수정 API 실패 - 권한 없음")
        @WithMockUser(username = "test@example.com")
        void updateTask_Fail_Unauthorized() throws Exception {
                // given
                UpdateTaskRequest request = UpdateTaskRequest.builder()
                                .title("수정된 태스크")
                                .build();

                when(taskService.updateTask(anyString(), anyLong(), any(UpdateTaskRequest.class)))
                                .thenThrow(new UnauthorizedAccessException("해당 태스크에 대한 권한이 없습니다."));

                // when & then
                mockMvc.perform(put("/api/tasks/1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("태스크 삭제 API 성공")
        @WithMockUser(username = "test@example.com")
        void deleteTask_Success() throws Exception {
                // given
                doNothing().when(taskService).deleteTask(anyString(), anyLong());

                // when & then
                mockMvc.perform(delete("/api/tasks/1")
                                .with(csrf()))
                                .andExpect(status().isOk());

                verify(taskService).deleteTask(anyString(), eq(1L));
        }

        @Test
        @DisplayName("태스크 삭제 API 실패 - 태스크 없음")
        @WithMockUser(username = "test@example.com")
        void deleteTask_Fail_NotFound() throws Exception {
                // given
                doThrow(new TaskNotFoundException("태스크를 찾을 수 없습니다."))
                                .when(taskService).deleteTask(anyString(), anyLong());

                // when & then
                mockMvc.perform(delete("/api/tasks/999")
                                .with(csrf()))
                                .andExpect(status().isNotFound());
        }
}
