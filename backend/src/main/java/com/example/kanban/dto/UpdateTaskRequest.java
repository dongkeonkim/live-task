package com.example.kanban.dto;

import com.example.kanban.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private Long order;
}
