package com.shaper.server.controller;

import com.shaper.server.model.dto.TodoDto;
import com.shaper.server.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TodoController {
    
    private final TodoService todoService;
    
    @GetMapping("/{id}")
    public ResponseEntity<TodoDto> getTodoById(@PathVariable Integer id) {
        try {
            TodoDto todo = todoService.getTodoById(id);
            return ResponseEntity.ok(todo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/hire/{hireId}")
    public ResponseEntity<List<TodoDto>> getTodosByHireId(@PathVariable UUID hireId) {
        try {
            List<TodoDto> todos = todoService.getTodosByHireId(hireId);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hr/{hrId}")
    public ResponseEntity<List<TodoDto>> getTodosByHrId(@PathVariable UUID hrId) {
        try {
            List<TodoDto> todos = todoService.getTodosByHrId(hrId);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<TodoDto> completeTodo(@PathVariable Integer id) {
        try {
            TodoDto completedTodo = todoService.completeTodo(id);
            return ResponseEntity.ok(completedTodo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @PostMapping("/{id}/reminder")
    public ResponseEntity<Void> sendReminder(@PathVariable Integer id) {
        try {
            todoService.sendReminder(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @PostMapping("/template/{templateId}/hire/{hireId}")
    public ResponseEntity<List<TodoDto>> createTodosFromTemplate(@PathVariable Integer templateId, @PathVariable UUID hireId) {
        try {
            List<TodoDto> todos = todoService.createTodosFromTemplate(templateId, hireId);
            return ResponseEntity.status(HttpStatus.CREATED).body(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/hire/{hireId}/progress")
    public ResponseEntity<Double> getProgressPercentage(@PathVariable UUID hireId) {
        try {
            double progress = todoService.calculateProgressPercentage(hireId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<TodoDto>> getOverdueTodos() {
        try {
            List<TodoDto> overdueTodos = todoService.getOverdueTodos();
            return ResponseEntity.ok(overdueTodos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}