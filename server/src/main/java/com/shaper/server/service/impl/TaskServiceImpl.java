package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.entity.Template;
import com.shaper.server.model.enums.TaskStatus;
import com.shaper.server.model.enums.TaskType;
import com.shaper.server.repository.TaskRepository;
import com.shaper.server.repository.TemplateRepository;
import com.shaper.server.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    
    private final TaskRepository taskRepository;
    private final TemplateRepository templateRepository;
    
    @Override
    @Transactional
    public Task createTask(Integer templateId, String title, String description, TaskType taskType,
                          boolean requiresSignature, String resourceUrl, LocalDateTime eventDate) {
        log.debug("Creating task for template ID: {}", templateId);
        
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new DataNotFoundException("Template not found with ID: " + templateId));
        
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setTaskType(taskType);
        task.setRequiresSignature(requiresSignature);
        task.setResourceUrl(resourceUrl);
        task.setEventDate(eventDate);
        task.setTemplate(template);
        task.setStatus(TaskStatus.PENDING);
        
        // Set order index as the next available index
        Integer nextOrderIndex = getNextOrderIndex(templateId);
        task.setOrderIndex(nextOrderIndex);
        
        Task savedTask = taskRepository.save(task);
        log.debug("Created task with ID: {} for template ID: {}", savedTask.getId(), templateId);
        
        return savedTask;
    }
    
    @Override
    @Transactional
    public Task updateTask(Integer taskId, String title, String description, TaskType taskType,
                          boolean requiresSignature, String resourceUrl, LocalDateTime eventDate) {
        log.debug("Updating task with ID: {}", taskId);
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + taskId));
        
        task.setTitle(title);
        task.setDescription(description);
        task.setTaskType(taskType);
        task.setRequiresSignature(requiresSignature);
        task.setResourceUrl(resourceUrl);
        task.setEventDate(eventDate);
        
        Task updatedTask = taskRepository.save(task);
        log.debug("Updated task with ID: {}", taskId);
        
        return updatedTask;
    }
    
    @Override
    public Task getTaskById(Integer taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + taskId));
    }
    
    @Override
    public List<Task> getTasksByTemplateId(Integer templateId) {
        return taskRepository.findByTemplate_IdOrderByOrderIndexAsc(templateId);
    }
    
    @Override
    @Transactional
    public void deleteTask(Integer taskId) {
        log.debug("Deleting task with ID: {}", taskId);
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + taskId));
        
        Integer templateId = task.getTemplate().getId();
        Integer deletedOrderIndex = task.getOrderIndex();
        
        // Delete the task
        taskRepository.delete(task);
        
        // Reorder remaining tasks to fill the gap
        List<Task> remainingTasks = taskRepository.findByTemplate_IdOrderByOrderIndexAsc(templateId);
        for (int i = 0; i < remainingTasks.size(); i++) {
            Task remainingTask = remainingTasks.get(i);
            if (remainingTask.getOrderIndex() > deletedOrderIndex) {
                remainingTask.setOrderIndex(remainingTask.getOrderIndex() - 1);
                taskRepository.save(remainingTask);
            }
        }
        
        log.debug("Deleted task with ID: {} and reordered remaining tasks", taskId);
    }
    
    @Override
    @Transactional
    public void reorderTasks(Integer templateId, List<Integer> taskIds) {
        log.debug("Reordering tasks for template ID: {}", templateId);
        
        // Validate that all task IDs belong to the template
        List<Task> templateTasks = taskRepository.findByTemplate_IdOrderByOrderIndexAsc(templateId);
        if (templateTasks.size() != taskIds.size()) {
            throw new IllegalArgumentException("Task count mismatch for reordering");
        }
        
        // Update order indices based on the new order
        for (int i = 0; i < taskIds.size(); i++) {
            Integer taskId = taskIds.get(i);
            Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + taskId));
            
            if (!task.getTemplate().getId().equals(templateId)) {
                throw new IllegalArgumentException("Task " + taskId + " does not belong to template " + templateId);
            }
            
            task.setOrderIndex(i + 1);
            taskRepository.save(task);
        }
        
        log.debug("Reordered {} tasks for template ID: {}", taskIds.size(), templateId);
    }
    
    @Override
    @Transactional
    public void moveTaskUp(Integer taskId) {
        log.debug("Moving task up with ID: {}", taskId);
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + taskId));
        
        Integer currentIndex = task.getOrderIndex();
        if (currentIndex <= 1) {
            log.debug("Task with ID: {} is already at the top", taskId);
            return; // Already at the top
        }
        
        // Find the task above this one
        List<Task> templateTasks = taskRepository.findByTemplate_IdOrderByOrderIndexAsc(task.getTemplate().getId());
        Task taskAbove = templateTasks.stream()
            .filter(t -> t.getOrderIndex().equals(currentIndex - 1))
            .findFirst()
            .orElse(null);
        
        if (taskAbove != null) {
            // Swap order indices
            task.setOrderIndex(currentIndex - 1);
            taskAbove.setOrderIndex(currentIndex);
            
            taskRepository.save(task);
            taskRepository.save(taskAbove);
            
            log.debug("Moved task with ID: {} up from index {} to {}", taskId, currentIndex, currentIndex - 1);
        }
    }
    
    @Override
    @Transactional
    public void moveTaskDown(Integer taskId) {
        log.debug("Moving task down with ID: {}", taskId);
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new DataNotFoundException("Task not found with ID: " + taskId));
        
        Integer currentIndex = task.getOrderIndex();
        long totalTasks = countTasksInTemplate(task.getTemplate().getId());
        
        if (currentIndex >= totalTasks) {
            log.debug("Task with ID: {} is already at the bottom", taskId);
            return; // Already at the bottom
        }
        
        // Find the task below this one
        List<Task> templateTasks = taskRepository.findByTemplate_IdOrderByOrderIndexAsc(task.getTemplate().getId());
        Task taskBelow = templateTasks.stream()
            .filter(t -> t.getOrderIndex().equals(currentIndex + 1))
            .findFirst()
            .orElse(null);
        
        if (taskBelow != null) {
            // Swap order indices
            task.setOrderIndex(currentIndex + 1);
            taskBelow.setOrderIndex(currentIndex);
            
            taskRepository.save(task);
            taskRepository.save(taskBelow);
            
            log.debug("Moved task with ID: {} down from index {} to {}", taskId, currentIndex, currentIndex + 1);
        }
    }
    
    @Override
    public Integer getNextOrderIndex(Integer templateId) {
        Integer maxIndex = taskRepository.findMaxOrderIndexByTemplateId(templateId);
        return maxIndex == null ? 1 : maxIndex + 1;
    }
    
    @Override
    public long countTasksInTemplate(Integer templateId) {
        return taskRepository.countByTemplateId(templateId);
    }
}