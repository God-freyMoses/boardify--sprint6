package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.dto.TemplateDTO;
import com.shaper.server.model.dto.TodoDto;
import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.TemplateStatus;
import com.shaper.server.repository.*;
import com.shaper.server.service.ProgressService;
import com.shaper.server.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceImplTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private HrUserRepository hrUserRepository;

    @Mock
    private CompanyDepartmentRepository departmentRepository;

    @Mock
    private HireRepository hireRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoService todoService;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private TemplateServiceImpl templateService;

    private Template testTemplate;
    private HrUser testHrUser;
    private Hire testHire;
    private TemplateDTO testTemplateDTO;
    private CompanyDepartment testDepartment;
    private UUID hrId;
    private UUID hireId;

    @BeforeEach
    void setUp() {
        hrId = UUID.randomUUID();
        hireId = UUID.randomUUID();

        testHrUser = new HrUser();
        testHrUser.setId(hrId);
        testHrUser.setFirstName("HR");
        testHrUser.setLastName("Manager");

        testHire = new Hire();
        testHire.setId(hireId);
        testHire.setFirstName("John");
        testHire.setLastName("Doe");

        testDepartment = new CompanyDepartment();
        testDepartment.setId(1);
        testDepartment.setName("Engineering");

        testTemplate = new Template();
        testTemplate.setId(1);
        testTemplate.setTitle("Test Template");
        testTemplate.setDescription("Test Description");
        testTemplate.setStatus(TemplateStatus.PENDING);
        testTemplate.setCreatedByHr(testHrUser);
        testTemplate.setCreatedDate(LocalDateTime.now());
        testTemplate.setUpdatedDate(LocalDateTime.now());

        // Add tasks to template
        Set<Task> tasks = new HashSet<>();
        Task task1 = new Task();
        task1.setId(1);
        task1.setTitle("Task 1");
        task1.setTemplate(testTemplate);
        tasks.add(task1);
        testTemplate.setTasks(tasks);

        testTemplateDTO = new TemplateDTO();
        testTemplateDTO.setTitle("Test Template");
        testTemplateDTO.setDescription("Test Description");
        testTemplateDTO.setHrId(hrId.toString());
        testTemplateDTO.setDepartmentIds(Arrays.asList(1));
    }

    @Test
    void createTemplate_ShouldCreateTemplateSuccessfully() {
        // Given
        when(hrUserRepository.findById(hrId)).thenReturn(Optional.of(testHrUser));
        when(departmentRepository.findAllById(Arrays.asList(1))).thenReturn(Arrays.asList(testDepartment));
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);

        // When
        TemplateDTO result = templateService.createTemplate(testTemplateDTO);

        // Then
        assertNotNull(result);
        assertEquals("Test Template", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        
        verify(hrUserRepository).findById(hrId);
        verify(departmentRepository).findAllById(Arrays.asList(1));
        verify(templateRepository).save(any(Template.class));
    }

    @Test
    void createTemplate_ShouldThrowException_WhenHrUserNotFound() {
        // Given
        when(hrUserRepository.findById(hrId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            templateService.createTemplate(testTemplateDTO));

        verify(hrUserRepository).findById(hrId);
        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    void createTemplate_ShouldThrowException_WhenDepartmentNotFound() {
        // Given
        when(hrUserRepository.findById(hrId)).thenReturn(Optional.of(testHrUser));
        when(departmentRepository.findAllById(Arrays.asList(1))).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            templateService.createTemplate(testTemplateDTO));

        verify(hrUserRepository).findById(hrId);
        verify(departmentRepository).findAllById(Arrays.asList(1));
        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    void updateTemplate_ShouldUpdateTemplateSuccessfully() {
        // Given
        testTemplateDTO.setTitle("Updated Template");
        testTemplateDTO.setStatus("IN_PROGRESS");
        
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(departmentRepository.findAllById(Arrays.asList(1))).thenReturn(Arrays.asList(testDepartment));
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);

        // When
        TemplateDTO result = templateService.updateTemplate(1, testTemplateDTO);

        // Then
        assertNotNull(result);
        verify(templateRepository).findById(1);
        verify(departmentRepository).findAllById(Arrays.asList(1));
        verify(templateRepository).save(testTemplate);
    }

    @Test
    void updateTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            templateService.updateTemplate(1, testTemplateDTO));

        verify(templateRepository).findById(1);
        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    void getTemplateById_ShouldReturnTemplate_WhenExists() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));

        // When
        TemplateDTO result = templateService.getTemplateById(1);

        // Then
        assertNotNull(result);
        assertEquals("Test Template", result.getTitle());
        verify(templateRepository).findById(1);
    }

    @Test
    void getTemplateById_ShouldThrowException_WhenNotFound() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            templateService.getTemplateById(1));

        verify(templateRepository).findById(1);
    }

    @Test
    void getAllTemplates_ShouldReturnAllTemplates() {
        // Given
        List<Template> templates = Arrays.asList(testTemplate);
        when(templateRepository.findAll()).thenReturn(templates);

        // When
        List<TemplateDTO> result = templateService.getAllTemplates();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Template", result.get(0).getTitle());
        verify(templateRepository).findAll();
    }

    @Test
    void getTemplatesByDepartmentId_ShouldReturnTemplatesForDepartment() {
        // Given
        List<Template> templates = Arrays.asList(testTemplate);
        when(templateRepository.findByDepartmentId(1)).thenReturn(templates);

        // When
        List<TemplateDTO> result = templateService.getTemplatesByDepartmentId(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(templateRepository).findByDepartmentId(1);
    }

    @Test
    void getTemplatesByHrId_ShouldReturnTemplatesForHrUser() {
        // Given
        List<Template> templates = Arrays.asList(testTemplate);
        when(templateRepository.findByCreatedByHr_Id(hrId)).thenReturn(templates);

        // When
        List<TemplateDTO> result = templateService.getTemplatesByHrId(hrId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(templateRepository).findByCreatedByHr_Id(hrId);
    }

    @Test
    void getTemplatesByCompanyId_ShouldReturnTemplatesForCompany() {
        // Given
        List<Template> templates = Arrays.asList(testTemplate);
        when(templateRepository.findByCompanyId(1)).thenReturn(templates);

        // When
        List<TemplateDTO> result = templateService.getTemplatesByCompanyId(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(templateRepository).findByCompanyId(1);
    }

    @Test
    void deleteTemplate_ShouldDeleteTemplate_WhenNoActiveTodos() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(todoRepository.findByTemplate_Id(1)).thenReturn(Collections.emptyList());

        // When
        templateService.deleteTemplate(1);

        // Then
        verify(templateRepository).findById(1);
        verify(todoRepository).findByTemplate_Id(1);
        verify(templateRepository).deleteById(1);
    }

    @Test
    void deleteTemplate_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            templateService.deleteTemplate(1));

        verify(templateRepository).findById(1);
        verify(templateRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteTemplate_ShouldThrowException_WhenHasActiveTodos() {
        // Given
        Todo activeTodo = new Todo();
        activeTodo.setId(1);
        
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(todoRepository.findByTemplate_Id(1)).thenReturn(Arrays.asList(activeTodo));

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            templateService.deleteTemplate(1));

        verify(templateRepository).findById(1);
        verify(todoRepository).findByTemplate_Id(1);
        verify(templateRepository, never()).deleteById(anyInt());
    }

    @Test
    void assignTemplateToHire_ShouldAssignSuccessfully() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(hireRepository.findById(hireId)).thenReturn(Optional.of(testHire));
        when(todoRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Collections.emptyList());
        
        List<TodoDto> createdTodos = Arrays.asList(new TodoDto());
        when(todoService.createTodosFromTemplate(1, hireId)).thenReturn(createdTodos);
        
        Progress mockProgress = new Progress();
        when(progressService.initializeProgress(hireId, 1)).thenReturn(mockProgress);

        // When
        templateService.assignTemplateToHire(1, hireId);

        // Then
        verify(templateRepository).findById(1);
        verify(hireRepository).findById(hireId);
        verify(todoRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(todoService).createTodosFromTemplate(1, hireId);
        verify(progressService).initializeProgress(hireId, 1);
        verify(templateRepository).save(testTemplate);
    }

    @Test
    void assignTemplateToHire_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            templateService.assignTemplateToHire(1, hireId));

        verify(templateRepository).findById(1);
        verify(todoService, never()).createTodosFromTemplate(anyInt(), any(UUID.class));
    }

    @Test
    void assignTemplateToHire_ShouldThrowException_WhenHireNotFound() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(hireRepository.findById(hireId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            templateService.assignTemplateToHire(1, hireId));

        verify(templateRepository).findById(1);
        verify(hireRepository).findById(hireId);
        verify(todoService, never()).createTodosFromTemplate(anyInt(), any(UUID.class));
    }

    @Test
    void assignTemplateToHire_ShouldThrowException_WhenTemplateHasNoTasks() {
        // Given
        testTemplate.setTasks(null);
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(hireRepository.findById(hireId)).thenReturn(Optional.of(testHire));

        // When & Then
        assertThrows(IllegalStateException.class, () -> 
            templateService.assignTemplateToHire(1, hireId));

        verify(templateRepository).findById(1);
        verify(hireRepository).findById(hireId);
        verify(todoService, never()).createTodosFromTemplate(anyInt(), any(UUID.class));
    }

    @Test
    void assignTemplateToHire_ShouldSkip_WhenAlreadyAssigned() {
        // Given
        Todo existingTodo = new Todo();
        existingTodo.setId(1);
        
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(hireRepository.findById(hireId)).thenReturn(Optional.of(testHire));
        when(todoRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Arrays.asList(existingTodo));

        // When
        templateService.assignTemplateToHire(1, hireId);

        // Then
        verify(templateRepository).findById(1);
        verify(hireRepository).findById(hireId);
        verify(todoRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(todoService, never()).createTodosFromTemplate(anyInt(), any(UUID.class));
        verify(progressService, never()).initializeProgress(any(UUID.class), anyInt());
    }

    @Test
    void assignTemplateToHire_ShouldUpdateStatusToInProgress_WhenPending() {
        // Given
        testTemplate.setStatus(TemplateStatus.PENDING);
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(hireRepository.findById(hireId)).thenReturn(Optional.of(testHire));
        when(todoRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Collections.emptyList());
        
        List<TodoDto> createdTodos = Arrays.asList(new TodoDto());
        when(todoService.createTodosFromTemplate(1, hireId)).thenReturn(createdTodos);
        
        Progress mockProgress = new Progress();
        when(progressService.initializeProgress(hireId, 1)).thenReturn(mockProgress);

        // When
        templateService.assignTemplateToHire(1, hireId);

        // Then
        assertEquals(TemplateStatus.IN_PROGRESS, testTemplate.getStatus());
        verify(templateRepository).save(testTemplate);
    }

    @Test
    void getTemplateEntityById_ShouldReturnTemplate_WhenExists() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));

        // When
        Template result = templateService.getTemplateEntityById(1);

        // Then
        assertNotNull(result);
        assertEquals(testTemplate, result);
        verify(templateRepository).findById(1);
    }

    @Test
    void getTemplateEntityById_ShouldThrowException_WhenNotFound() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            templateService.getTemplateEntityById(1));

        verify(templateRepository).findById(1);
    }
}