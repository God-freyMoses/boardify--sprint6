package com.shaper.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Integer id;
    private String name;
    private String filePath;
    private String downloadUrl;
    private boolean requiresSignature;
    private Integer taskId;
    private String taskTitle;
    private LocalDateTime uploadedAt;
    private long fileSize;
    private String contentType;
}