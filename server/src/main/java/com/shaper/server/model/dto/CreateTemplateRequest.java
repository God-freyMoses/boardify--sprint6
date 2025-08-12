package com.shaper.server.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTemplateRequest {

    @NotBlank(message = "Template title is required")
    @Size(min = 2, max = 255, message = "Template title must be between 2 and 255 characters")
    private String title;

    @Size(max = 1000, message = "Template description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "HR user ID is required")
    private UUID hrId;

    private List<Integer> departmentIds;
}