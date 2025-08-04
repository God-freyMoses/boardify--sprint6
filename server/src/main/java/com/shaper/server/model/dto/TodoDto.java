package com.shaper.server.model.dto;

import com.shaper.server.model.entity.Todo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoDto {
    private Integer id;
    private UUID hireId;
    private String hireName;
    private Integer taskId;
    private String taskTitle;
    private String taskDescription;
    private String taskType; // EVENT, DOCUMENT, RESOURCE
    private boolean requiresSignature;
    private Integer templateId;
    private String templateTitle;
    private Todo.TodoStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime dueDate;
    private LocalDateTime reminderSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}