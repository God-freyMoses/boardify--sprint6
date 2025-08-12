package com.shaper.server.service;

import com.shaper.server.model.dto.TodoDto;
import com.shaper.server.model.entity.Todo;
import com.shaper.server.model.enums.TodoStatus;

import java.util.List;
import java.util.UUID;

public interface TodoService {
    
    TodoDto getTodoById(Integer id);
    
    List<TodoDto> getTodosByHireId(UUID hireId);
    
    List<TodoDto> getTodosByHrId(UUID hrId);
    
    TodoDto completeTodo(Integer id);
    
    void sendReminder(Integer id);
    
    List<TodoDto> createTodosFromTemplate(Integer templateId, UUID hireId);
    
    double calculateProgressPercentage(UUID hireId);
    
    Todo getTodoEntityById(Integer id);
    
    List<TodoDto> getOverdueTodos();
    
    // Enhanced filtering methods
    List<TodoDto> getTodosByHireIdAndStatus(UUID hireId, TodoStatus status);
    List<TodoDto> getTodosByStatus(TodoStatus status);
    List<TodoDto> getTodosByTemplateId(Integer templateId);
    List<TodoDto> getPendingTodosByHireId(UUID hireId);
    List<TodoDto> getCompletedTodosByHireId(UUID hireId);
    
    // Bulk operations
    List<TodoDto> markMultipleTodosComplete(List<Integer> todoIds);
    
    // Status transition methods
    TodoDto markTodoInProgress(Integer id);
    TodoDto markTodoOverdue(Integer id);
}