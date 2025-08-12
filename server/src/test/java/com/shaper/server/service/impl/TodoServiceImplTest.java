package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.dto.TodoDto;
import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.TodoStatus;
import com.shaper.server.model.enums.TaskType;
import com.shaper.server.repository.*;
import com.shaper.server.service.NotificationService;
import com.shaper.server.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;
    
    @Mock
    private TemplateRepository templateRepository;
    
    @Mock
    private HireRepository hireRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private ProgressService progressService;
    
    @InjectMocks
    private TodoServiceImpl todoService;
    
    private Todo testTodo;
    private Hire testHire;
    private Template testTemplate;
    private Task testTask;
    private UUID hireId;
    
    @BeforeEach
    void setUp() {
        hireId = UUID.randomUUID();
        
        testHire = new Hire();
        testHire.setId(hireId);
        testHire.setFirstName("John");
        testHire.setLastName("Doe");
        
        testTemplate = new Template();
        testTemplate.setId(1);
        testTemplate.setTitle("Test Template");
        
        testTask = new Task();
        testTask.setId(1);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTaskType(TaskType.DOCUMENT);
        testTask.setRequiresSignature(false);
        testTask.setTemplate(testTemplate);
        
        testTodo = new Todo();
        testTodo.setId(1);
        testTodo.setHire(testHire);
        testTodo.setTask(testTask);
        testTodo.setTemplate(testTemplate);
        testTodo.setStatus(TodoStatus.PENDING);
        testTodo.setDueDate(LocalDateTime.now().plusDays(7));
        testTodo.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    void getTodoById_ShouldReturnTodoDto_WhenTodoExists() {
        // Given
        when(todoRepository.findById(1)).thenReturn(Optional.of(testTodo));
        
        // When
        TodoDto result = todoService.getTodoById(1);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getHireId()).isEqualTo(hireId);
        assertThat(result.getTaskTitle()).isEqualTo("Test Task");
        verify(todoRepository).findById(1);
    }
    
    @Test
    void getTodoById_ShouldThrowException_WhenTodoNotFound() {
        // Given
        when(todoRepository.findById(1)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> todoService.getTodoById(1))
            .isInstanceOf(DataNotFoundException.class)
            .hasMessageContaining("Todo not found with ID: 1");
        
        verify(todoRepository).findById(1);
    }
    
    @Test
    void getTodosByHireId_ShouldReturnTodoList() {
        // Given
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoRepository.findByHire_IdOrderByCreatedAtAsc(hireId)).thenReturn(todos);
        
        // When
        List<TodoDto> result = todoService.getTodosByHireId(hireId);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHireId()).isEqualTo(hireId);
        verify(todoRepository).findByHire_IdOrderByCreatedAtAsc(hireId);
    }
    
    @Test
    void completeTodo_ShouldMarkTodoAsCompleted() {
        // Given
        when(todoRepository.findById(1)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
        when(progressService.updateProgressOnTodoCompletion(1)).thenReturn(new Progress());
        
        // When
        TodoDto result = todoService.completeTodo(1);
        
        // Then
        assertThat(result).isNotNull();
        verify(todoRepository).findById(1);
        verify(todoRepository).save(testTodo);
        verify(progressService).updateProgressOnTodoCompletion(1);
        assertThat(testTodo.getStatus()).isEqualTo(TodoStatus.COMPLETED);
        assertThat(testTodo.getCompletedAt()).isNotNull();
    }
    
    @Test
    void completeTodo_ShouldCreateNotification_WhenTaskRequiresSignature() {
        // Given
        testTask.setRequiresSignature(true);
        HrUser hrUser = new HrUser();
        testHire.setRegisteredByHr(hrUser);
        
        when(todoRepository.findById(1)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
        when(progressService.updateProgressOnTodoCompletion(1)).thenReturn(new Progress());
        
        // When
        todoService.completeTodo(1);
        
        // Then
        verify(notificationService).createDocumentSignedNotification(hrUser, testTask);
    }
    
    @Test
    void completeTodo_ShouldReturnSameTodo_WhenAlreadyCompleted() {
        // Given
        testTodo.setStatus(TodoStatus.COMPLETED);
        testTodo.setCompletedAt(LocalDateTime.now());
        when(todoRepository.findById(1)).thenReturn(Optional.of(testTodo));
        
        // When
        TodoDto result = todoService.completeTodo(1);
        
        // Then
        assertThat(result).isNotNull();
        verify(todoRepository).findById(1);
        verify(todoRepository, never()).save(any(Todo.class));
        verify(progressService, never()).updateProgressOnTodoCompletion(anyInt());
    }
    
    @Test
    void sendReminder_ShouldUpdateReminderSentAt() {
        // Given
        when(todoRepository.findById(1)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
        
        // When
        todoService.sendReminder(1);
        
        // Then
        verify(todoRepository).findById(1);
        verify(todoRepository).save(testTodo);
        verify(notificationService).createReminderNotification(testHire, testTask);
        assertThat(testTodo.getReminderSentAt()).isNotNull();
    }
    
    @Test
    void createTodosFromTemplate_ShouldCreateTodosFromTasks() {
        // Given
        Set<Task> tasks = new HashSet<>();
        Task task1 = new Task();
        task1.setId(1);
        task1.setTitle("Task 1");
        task1.setOrderIndex(1);
        task1.setTemplate(testTemplate);
        tasks.add(task1);
        
        Task task2 = new Task();
        task2.setId(2);
        task2.setTitle("Task 2");
        task2.setOrderIndex(2);
        task2.setTemplate(testTemplate);
        tasks.add(task2);
        
        testTemplate.setTasks(tasks);
        
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(hireRepository.findById(hireId)).thenReturn(Optional.of(testHire));
        when(todoRepository.saveAll(anyList())).thenReturn(Arrays.asList(testTodo, testTodo));
        
        // When
        List<TodoDto> result = todoService.createTodosFromTemplate(1, hireId);
        
        // Then
        assertThat(result).hasSize(2);
        verify(templateRepository).findById(1);
        verify(hireRepository).findById(hireId);
        verify(todoRepository).saveAll(anyList());
    }
    
    @Test
    void calculateProgressPercentage_ShouldReturnCorrectPercentage() {
        // Given
        when(todoRepository.countByHireId(hireId)).thenReturn(4L);
        when(todoRepository.countByHireIdAndStatus(hireId, TodoStatus.COMPLETED)).thenReturn(3L);
        
        // When
        double result = todoService.calculateProgressPercentage(hireId);
        
        // Then
        assertThat(result).isEqualTo(75.0);
        verify(todoRepository).countByHireId(hireId);
        verify(todoRepository).countByHireIdAndStatus(hireId, TodoStatus.COMPLETED);
    }
    
    @Test
    void calculateProgressPercentage_ShouldReturnZero_WhenNoTodos() {
        // Given
        when(todoRepository.countByHireId(hireId)).thenReturn(0L);
        
        // When
        double result = todoService.calculateProgressPercentage(hireId);
        
        // Then
        assertThat(result).isEqualTo(0.0);
        verify(todoRepository).countByHireId(hireId);
        verify(todoRepository, never()).countByHireIdAndStatus(any(), any());
    }
    
    @Test
    void getOverdueTodos_ShouldReturnOverdueTodos() {
        // Given
        List<Todo> overdueTodos = Arrays.asList(testTodo);
        when(todoRepository.findOverdueTodos(eq(TodoStatus.PENDING), any(LocalDateTime.class)))
            .thenReturn(overdueTodos);
        
        // When
        List<TodoDto> result = todoService.getOverdueTodos();
        
        // Then
        assertThat(result).hasSize(1);
        verify(todoRepository).findOverdueTodos(eq(TodoStatus.PENDING), any(LocalDateTime.class));
    }
}