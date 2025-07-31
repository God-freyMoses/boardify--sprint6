package com.shaper.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateDTO {

    private Integer id;
    private String title;
    private String description;
    private String status;
    private Integer hrId;
    private String hrName;
    private List<Integer> taskIds;
    private List<Integer> departmentIds;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}