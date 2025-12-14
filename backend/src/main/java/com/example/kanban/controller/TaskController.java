package com.example.kanban.controller;

import com.example.kanban.dto.CreateTaskRequest;
import com.example.kanban.dto.TaskDto;
import com.example.kanban.dto.UpdateTaskRequest;
import com.example.kanban.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTasks(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(userDetails.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        taskService.deleteTask(userDetails.getUsername(), id);
        return ResponseEntity.ok().build();
    }
}
