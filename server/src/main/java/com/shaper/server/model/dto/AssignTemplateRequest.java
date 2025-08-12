package com.shaper.server.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignTemplateRequest {

    @NotNull(message = "Template ID is required")
    private Integer templateId;

    @NotEmpty(message = "At least one hire ID is required")
    private List<UUID> hireIds;
}