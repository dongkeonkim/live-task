package com.example.kanban.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<TaskDto> getTasks(String username) {
        User user = getUser(username);
        return taskRepository.findAllByUserOrderByOrderAsc(user).stream()
                .map(this::mapToDto)
                .toList();
    }

    public TaskDto createTask(String username, CreateTaskRequest request) {
        User user = getUser(username);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .order(System.currentTimeMillis())
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToDto(savedTask);
    }

    public TaskDto updateTask(String username, Long taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다."));

        if (!task.getUser().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("해당 태스크에 대한 권한이 없습니다.");
        }

        if (request.getTitle() != null)
            task.setTitle(request.getTitle());
        if (request.getDescription() != null)
            task.setDescription(request.getDescription());
        if (request.getStatus() != null)
            task.setStatus(request.getStatus());
        if (request.getOrder() != null)
            task.setOrder(request.getOrder());

        return mapToDto(taskRepository.save(task));
    }

    public void deleteTask(String username, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("태스크를 찾을 수 없습니다."));

        if (!task.getUser().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("해당 태스크에 대한 권한이 없습니다.");
        }

        taskRepository.delete(task);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private TaskDto mapToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .order(task.getOrder())
                .creatorName(task.getUser().getName())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
