package com.shaper.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaper.server.model.dto.TodoDto;
import com.shaper.server.model.enums.TodoStatus;
import com.shaper.server.service.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
class TodoControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TodoService todoService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void getTodoById_ShouldReturnTodo_WhenTodoExists() throws Exception {
        // Given
        TodoDto todoDto = new TodoDto();
        todoDto.setId(1);
        todoDto.setTaskTitle("Test Todo");
        when(todoService.getTodoById(1)).thenReturn(todoDto);
        
        // When & Then
        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Todo"));
    }
    
    @Test
    void completeTodo_ShouldReturnCompletedTodo() throws Exception {
        // Given
        TodoDto completedTodo = new TodoDto();
        completedTodo.setId(1);
        completedTodo.setStatus(TodoStatus.COMPLETED);
        when(todoService.completeTodo(1)).thenReturn(completedTodo);
        
        // When & Then
        mockMvc.perform(put("/api/todos/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void sendReminder_ShouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/todos/1/reminder"))
                .andExpect(status().isOk());
    }
    
    @Test
    void getTodosByHireId_ShouldReturnTodoList() throws Exception {
        // Given
        UUID hireId = UUID.randomUUID();
        List<TodoDto> todos = Arrays.asList(new TodoDto(), new TodoDto());
        when(todoService.getTodosByHireId(hireId)).thenReturn(todos);
        
        // When & Then
        mockMvc.perform(get("/api/todos/hire/" + hireId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void getProgressPercentage_ShouldReturnPercentage() throws Exception {
        // Given
        UUID hireId = UUID.randomUUID();
        when(todoService.calculateProgressPercentage(hireId)).thenReturn(75.0);
        
        // When & Then
        mockMvc.perform(get("/api/todos/hire/" + hireId + "/progress"))
                .andExpect(status().isOk())
                .andExpect(content().string("75.0"));
    }
    
    @Test
    void getOverdueTodos_ShouldReturnOverdueTodos() throws Exception {
        // Given
        List<TodoDto> overdueTodos = Arrays.asList(new TodoDto());
        when(todoService.getOverdueTodos()).thenReturn(overdueTodos);
        
        // When & Then
        mockMvc.perform(get("/api/todos/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
    
    @Test
    void markMultipleTodosComplete_ShouldReturnCompletedTodos() throws Exception {
        // Given
        List<Integer> todoIds = Arrays.asList(1, 2, 3);
        List<TodoDto> completedTodos = Arrays.asList(new TodoDto(), new TodoDto(), new TodoDto());
        when(todoService.markMultipleTodosComplete(any())).thenReturn(completedTodos);
        
        // When & Then
        mockMvc.perform(put("/api/todos/bulk-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todoIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}