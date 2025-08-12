package com.shaper.server.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReorderTasksRequest {

    @NotNull(message = "Template ID is required")
    private Integer templateId;

    @NotEmpty(message = "Task IDs list cannot be empty")
    private List<Integer> taskIds;
}