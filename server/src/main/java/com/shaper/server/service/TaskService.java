package com.shaper.server.service;

import com.shaper.server.model.entity.Task;
import com.shaper.server.model.enums.TaskType;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {
    
    /**
     * Create a new task and add it to a template
     */
    Task createTask(Integer templateId, String title, String description, TaskType taskType, 
                   boolean requiresSignature, String resourceUrl, LocalDateTime eventDate);
    
    /**
     * Update an existing task
     */
    Task updateTask(Integer taskId, String title, String description, TaskType taskType,
                   boolean requiresSignature, String resourceUrl, LocalDateTime eventDate);
    
    /**
     * Get task by ID
     */
    Task getTaskById(Integer taskId);
    
    /**
     * Get all tasks for a template ordered by index
     */
    List<Task> getTasksByTemplateId(Integer templateId);
    
    /**
     * Delete a task from template
     */
    void deleteTask(Integer taskId);
    
    /**
     * Reorder tasks within a template
     */
    void reorderTasks(Integer templateId, List<Integer> taskIds);
    
    /**
     * Move task up in order
     */
    void moveTaskUp(Integer taskId);
    
    /**
     * Move task down in order
     */
    void moveTaskDown(Integer taskId);
    
    /**
     * Get the next order index for a template
     */
    Integer getNextOrderIndex(Integer templateId);
    
    /**
     * Count tasks in a template
     */
    long countTasksInTemplate(Integer templateId);
}