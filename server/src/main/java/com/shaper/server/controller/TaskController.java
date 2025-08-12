package com.shaper.server.controller;

import com.shaper.server.model.dto.*;
import com.shaper.server.model.entity.Task;
import com.shaper.server.service.TaskService;
import com.shaper.server.system.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * Create a new task for a template
     */
    @PostMapping("/templates/{templateId}")
    public ResponseEntity<Result> createTask(@PathVariable Integer templateId, @Valid @RequestBody CreateTaskRequest request) {
        log.debug("Creating task for template ID: {}", templateId);
        
        Task createdTask = taskService.createTask(
            templateId,
            request.getTitle(),
            request.getDescription(),
            request.getTaskType(),
            request.isRequiresSignature(),
            request.getResourceUrl(),
            request.getEventDate()
        );
        
        TaskDTO taskDto = convertToDto(createdTask);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new Result(HttpStatus.CREATED.value(), true, "Task created successfully", taskDto));
    }
    
    /**
     * Update an existing task
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result> updateTask(@PathVariable Integer id, @Valid @RequestBody UpdateTaskRequest request) {
        log.debug("Updating task with ID: {}", id);
        
        Task updatedTask = taskService.updateTask(
            id,
            request.getTitle(),
            request.getDescription(),
            request.getTaskType(),
            request.isRequiresSignature(),
            request.getResourceUrl(),
            request.getEventDate()
        );
        
        TaskDTO taskDto = convertToDto(updatedTask);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Task updated successfully", taskDto));
    }
    
    /**
     * Get task by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result> getTaskById(@PathVariable Integer id) {
        log.debug("Getting task with ID: {}", id);
        Task task = taskService.getTaskById(id);
        TaskDTO taskDto = convertToDto(task);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Task retrieved successfully", taskDto));
    }
    
    /**
     * Get all tasks for a template
     */
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<Result> getTasksByTemplateId(@PathVariable Integer templateId) {
        log.debug("Getting tasks for template ID: {}", templateId);
        List<Task> tasks = taskService.getTasksByTemplateId(templateId);
        List<TaskDTO> taskDtos = tasks.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Tasks retrieved successfully", taskDtos));
    }
    
    /**
     * Delete a task
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteTask(@PathVariable Integer id) {
        log.debug("Deleting task with ID: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Task deleted successfully"));
    }
    
    /**
     * Reorder tasks within a template
     */
    @PutMapping("/reorder")
    public ResponseEntity<Result> reorderTasks(@Valid @RequestBody ReorderTasksRequest request) {
        log.debug("Reordering tasks for template ID: {}", request.getTemplateId());
        taskService.reorderTasks(request.getTemplateId(), request.getTaskIds());
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Tasks reordered successfully"));
    }
    
    /**
     * Move task up in order
     */
    @PutMapping("/{id}/move-up")
    public ResponseEntity<Result> moveTaskUp(@PathVariable Integer id) {
        log.debug("Moving task up with ID: {}", id);
        taskService.moveTaskUp(id);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Task moved up successfully"));
    }
    
    /**
     * Move task down in order
     */
    @PutMapping("/{id}/move-down")
    public ResponseEntity<Result> moveTaskDown(@PathVariable Integer id) {
        log.debug("Moving task down with ID: {}", id);
        taskService.moveTaskDown(id);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Task moved down successfully"));
    }
    
    /**
     * Get task count for a template
     */
    @GetMapping("/templates/{templateId}/count")
    public ResponseEntity<Result> getTaskCount(@PathVariable Integer templateId) {
        log.debug("Getting task count for template ID: {}", templateId);
        long count = taskService.countTasksInTemplate(templateId);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Task count retrieved successfully", count));
    }
    
    private TaskDTO convertToDto(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setTaskType(task.getTaskType());
        dto.setStatus(task.getStatus());
        dto.setRequiresSignature(task.isRequiresSignature());
        dto.setResourceUrl(task.getResourceUrl());
        dto.setEventDate(task.getEventDate());
        dto.setOrderIndex(task.getOrderIndex());
        dto.setTemplateId(task.getTemplate().getId());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }
}