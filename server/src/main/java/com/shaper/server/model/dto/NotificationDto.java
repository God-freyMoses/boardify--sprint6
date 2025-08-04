package com.shaper.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Integer id;
    private UUID userId;
    private String userName;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Integer relatedTaskId;
    private String relatedTaskTitle;
    private String notificationType; // TASK_COMPLETED, DOCUMENT_SIGNED, REMINDER, etc.
}