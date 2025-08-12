package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.dto.TodoDto;
import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.TodoStatus;
import com.shaper.server.repository.*;
import com.shaper.server.service.NotificationService;
import com.shaper.server.service.ProgressService;
import com.shaper.server.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoServiceImpl implements TodoService {
    
    private final TodoRepository todoRepository;
    private final TemplateRepository templateRepository;
    private final HireRepository hireRepository;
    private final NotificationService notificationService;
    private final ProgressService progressService;
    
    @Override
    public TodoDto getTodoById(Integer id) {
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Todo not found with ID: " + id));
        return convertToDto(todo);
    }
    
    @Override
    public List<TodoDto> getTodosByHireId(UUID hireId) {
        List<Todo> todos = todoRepository.findByHire_IdOrderByCreatedAtAsc(hireId);
        return todos.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public List<TodoDto> getTodosByHrId(UUID hrId) {
        List<Todo> todos = todoRepository.findByHrId(hrId);
        return todos.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public TodoDto completeTodo(Integer id) {
        log.debug("Completing todo with ID: {}", id);
        
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Todo not found with ID: " + id));
        
        if (todo.getStatus() == TodoStatus.COMPLETED) {
            log.debug("Todo with ID: {} is already completed", id);
            return convertToDto(todo);
        }
        
        todo.setStatus(TodoStatus.COMPLETED);
        todo.setCompletedAt(LocalDateTime.now());
        
        Todo savedTodo = todoRepository.save(todo);
        log.debug("Marked todo with ID: {} as completed", id);
        
        // Update progress tracking
        progressService.updateProgressOnTodoCompletion(id);
        log.debug("Updated progress for todo completion: {}", id);
        
        // Create notification for HR if task requires signature
        if (todo.getTask().isRequiresSignature()) {
            notificationService.createDocumentSignedNotification(
                todo.getHire().getRegisteredByHr(), 
                todo.getTask()
            );
            log.debug("Created signature notification for HR user: {}", 
                     todo.getHire().getRegisteredByHr().getId());
        }
        
        return convertToDto(savedTodo);
    }
    
    @Override
    @Transactional
    public void sendReminder(Integer id) {
        log.debug("Sending reminder for todo with ID: {}", id);
        
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Todo not found with ID: " + id));
        
        todo.setReminderSentAt(LocalDateTime.now());
        todoRepository.save(todo);
        
        // Create reminder notification
        notificationService.createReminderNotification(todo.getHire(), todo.getTask());
        log.debug("Sent reminder for todo with ID: {}", id);
    }
    
    @Override
    @Transactional
    public List<TodoDto> createTodosFromTemplate(Integer templateId, UUID hireId) {
        log.debug("Creating todos from template ID: {} for hire ID: {}", templateId, hireId);
        
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new DataNotFoundException("Template not found with ID: " + templateId));
        
        Hire hire = hireRepository.findById(hireId)
            .orElseThrow(() -> new DataNotFoundException("Hire not found with ID: " + hireId));
        
        if (template.getTasks() == null || template.getTasks().isEmpty()) {
            log.warn("Template ID: {} has no tasks to create todos from", templateId);
            return List.of();
        }
        
        // Create todos from template tasks, maintaining order
        List<Todo> todos = template.getTasks().stream()
            .sorted((t1, t2) -> Integer.compare(t1.getOrderIndex(), t2.getOrderIndex()))
            .map(task -> {
                Todo todo = new Todo();
                todo.setHire(hire);
                todo.setTask(task);
                todo.setTemplate(template);
                todo.setStatus(TodoStatus.PENDING);
                
                // Set due date based on task type and event date
                if (task.getEventDate() != null) {
                    todo.setDueDate(task.getEventDate());
                } else if (task.getDueDate() != null) {
                    todo.setDueDate(task.getDueDate());
                } else {
                    // Default due date: 7 days from now for documents, 14 days for resources
                    int daysToAdd = task.isRequiresSignature() ? 7 : 14;
                    todo.setDueDate(LocalDateTime.now().plusDays(daysToAdd));
                }
                
                return todo;
            }).collect(Collectors.toList());
        
        List<Todo> savedTodos = todoRepository.saveAll(todos);
        log.debug("Created {} todos from template ID: {} for hire ID: {}", 
                 savedTodos.size(), templateId, hireId);
        
        return savedTodos.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public double calculateProgressPercentage(UUID hireId) {
        long totalTodos = todoRepository.countByHireId(hireId);
        if (totalTodos == 0) {
            return 0.0;
        }
        
        long completedTodos = todoRepository.countByHireIdAndStatus(hireId, com.shaper.server.model.enums.TodoStatus.COMPLETED);
        return (double) completedTodos / totalTodos * 100.0;
    }
    
    @Override
    public Todo getTodoEntityById(Integer id) {
        return todoRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Todo not found with ID: " + id));
    }
    
    @Override
    public List<TodoDto> getOverdueTodos() {
        List<Todo> overdueTodos = todoRepository.findOverdueTodos(
            TodoStatus.PENDING, 
            LocalDateTime.now()
        );
        return overdueTodos.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    private TodoDto convertToDto(Todo todo) {
        TodoDto dto = new TodoDto();
        dto.setId(todo.getId());
        dto.setHireId(todo.getHire().getId());
        dto.setHireName(todo.getHire().getFirstName() + " " + todo.getHire().getLastName());
        dto.setTaskId(todo.getTask().getId());
        dto.setTaskTitle(todo.getTask().getTitle());
        dto.setTaskDescription(todo.getTask().getDescription());
        dto.setRequiresSignature(todo.getTask().isRequiresSignature());
        dto.setTemplateId(todo.getTemplate().getId());
        dto.setTemplateTitle(todo.getTemplate().getTitle());
        dto.setStatus(todo.getStatus());
        dto.setCompletedAt(todo.getCompletedAt());
        dto.setDueDate(todo.getDueDate());
        dto.setReminderSentAt(todo.getReminderSentAt());
        dto.setCreatedAt(todo.getCreatedAt());
        dto.setUpdatedAt(todo.getUpdatedAt());
        
        // Set task type from the task entity
        if (todo.getTask().getTaskType() != null) {
            dto.setTaskType(todo.getTask().getTaskType().name());
        } else {
            // Fallback logic for backward compatibility
            if (todo.getTask().isRequiresSignature()) {
                dto.setTaskType("DOCUMENT");
            } else if (todo.getTask().getEventDate() != null) {
                dto.setTaskType("EVENT");
            } else {
                dto.setTaskType("RESOURCE");
            }
        }
        
        return dto;
    }
    
    @Override
    public List<TodoDto> getTodosByHireIdAndStatus(UUID hireId, TodoStatus status) {
        List<Todo> todos = todoRepository.findByHire_IdAndStatusOrderByDueDateAsc(hireId, status);
        return todos.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public List<TodoDto> getTodosByStatus(TodoStatus status) {
        List<Todo> todos = todoRepository.findByStatusOrderByDueDateAsc(status);
        return todos.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public List<TodoDto> getTodosByTemplateId(Integer templateId) {
        List<Todo> todos = todoRepository.findByTemplate_Id(templateId);
        return todos.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public List<TodoDto> getPendingTodosByHireId(UUID hireId) {
        return getTodosByHireIdAndStatus(hireId, TodoStatus.PENDING);
    }
    
    @Override
    public List<TodoDto> getCompletedTodosByHireId(UUID hireId) {
        return getTodosByHireIdAndStatus(hireId, TodoStatus.COMPLETED);
    }
    
    @Override
    @Transactional
    public List<TodoDto> markMultipleTodosComplete(List<Integer> todoIds) {
        log.debug("Marking {} todos as complete", todoIds.size());
        
        List<TodoDto> completedTodos = new ArrayList<>();
        for (Integer todoId : todoIds) {
            try {
                TodoDto completed = completeTodo(todoId);
                completedTodos.add(completed);
            } catch (Exception e) {
                log.warn("Failed to complete todo with ID: {}", todoId, e);
            }
        }
        
        return completedTodos;
    }
    
    @Override
    @Transactional
    public TodoDto markTodoInProgress(Integer id) {
        log.debug("Marking todo with ID: {} as in progress", id);
        
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Todo not found with ID: " + id));
        
        todo.setStatus(TodoStatus.IN_PROGRESS);
        Todo savedTodo = todoRepository.save(todo);
        
        return convertToDto(savedTodo);
    }
    
    @Override
    @Transactional
    public TodoDto markTodoOverdue(Integer id) {
        log.debug("Marking todo with ID: {} as overdue", id);
        
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Todo not found with ID: " + id));
        
        todo.setStatus(TodoStatus.OVERDUE);
        Todo savedTodo = todoRepository.save(todo);
        
        return convertToDto(savedTodo);
    }
}