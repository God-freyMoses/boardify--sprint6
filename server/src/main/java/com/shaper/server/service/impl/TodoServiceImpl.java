package com.shaper.server.service.impl;

import com.shaper.server.model.dto.TodoDto;
import com.shaper.server.model.entity.*;
import com.shaper.server.repository.*;
import com.shaper.server.service.NotificationService;
import com.shaper.server.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
    
    private final TodoRepository todoRepository;
    private final TemplateRepository templateRepository;
    private final HireRepository hireRepository;
    private final NotificationService notificationService;
    
    @Override
    public TodoDto getTodoById(Integer id) {
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Todo not found"));
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
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Todo not found"));
        
        todo.setStatus(Todo.TodoStatus.COMPLETED);
        todo.setCompletedAt(LocalDateTime.now());
        
        Todo savedTodo = todoRepository.save(todo);
        
        // Create notification for HR if task requires signature
        if (todo.getTask().isRequiresSignature()) {
            notificationService.createDocumentSignedNotification(
                todo.getHire().getRegisteredByHr(), 
                todo.getTask()
            );
        }
        
        return convertToDto(savedTodo);
    }
    
    @Override
    @Transactional
    public void sendReminder(Integer id) {
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Todo not found"));
        
        todo.setReminderSentAt(LocalDateTime.now());
        todoRepository.save(todo);
        
        // Create reminder notification
        notificationService.createReminderNotification(todo.getHire(), todo.getTask());
    }
    
    @Override
    @Transactional
    public List<TodoDto> createTodosFromTemplate(Integer templateId, UUID hireId) {
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));
        
        Hire hire = hireRepository.findById(hireId)
            .orElseThrow(() -> new RuntimeException("Hire not found"));
        
        List<Todo> todos = template.getTasks().stream().map(task -> {
            Todo todo = new Todo();
            todo.setHire(hire);
            todo.setTask(task);
            todo.setTemplate(template);
            todo.setStatus(Todo.TodoStatus.PENDING);
            todo.setDueDate(task.getDueDate());
            return todo;
        }).collect(Collectors.toList());
        
        List<Todo> savedTodos = todoRepository.saveAll(todos);
        return savedTodos.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public double calculateProgressPercentage(UUID hireId) {
        long totalTodos = todoRepository.countByHireId(hireId);
        if (totalTodos == 0) {
            return 0.0;
        }
        
        long completedTodos = todoRepository.countByHireIdAndStatus(hireId, Todo.TodoStatus.COMPLETED);
        return (double) completedTodos / totalTodos * 100.0;
    }
    
    @Override
    public Todo getTodoEntityById(Integer id) {
        return todoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Todo not found"));
    }
    
    @Override
    public List<TodoDto> getOverdueTodos() {
        List<Todo> overdueTodos = todoRepository.findOverdueTodos(
            Todo.TodoStatus.PENDING, 
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
        
        // Determine task type based on task properties
        if (todo.getTask().isRequiresSignature()) {
            dto.setTaskType("DOCUMENT");
        } else if (todo.getTask().getDueDate() != null) {
            dto.setTaskType("EVENT");
        } else {
            dto.setTaskType("RESOURCE");
        }
        
        return dto;
    }
}