package com.shaper.server.model.dto;

import com.shaper.server.model.enums.TaskStatus;
import com.shaper.server.model.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {

    private Integer id;
    private String title;
    private String description;
    private TaskType taskType;
    private TaskStatus status;
    private boolean requiresSignature;
    private String resourceUrl;
    private LocalDateTime eventDate;
    private Integer orderIndex;
    private Integer templateId;
    private LocalDateTime createdAt;
}