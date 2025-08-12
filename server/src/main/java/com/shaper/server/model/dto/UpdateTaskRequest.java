package com.shaper.server.model.dto;

import com.shaper.server.model.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 255, message = "Task title must be between 2 and 255 characters")
    private String title;

    @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Task type is required")
    private TaskType taskType;

    private boolean requiresSignature = false;

    @Size(max = 500, message = "Resource URL cannot exceed 500 characters")
    private String resourceUrl;

    private LocalDateTime eventDate;
}