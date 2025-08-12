package com.shaper.server.controller;

import com.shaper.server.model.dto.*;
import com.shaper.server.service.TemplateService;
import com.shaper.server.system.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TemplateControllerTest {

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private TemplateController templateController;

    private TemplateDTO testTemplateDTO;
    private UUID testHrId;

    @BeforeEach
    void setUp() {
        testHrId = UUID.randomUUID();
        
        testTemplateDTO = new TemplateDTO();
        testTemplateDTO.setId(1);
        testTemplateDTO.setTitle("Test Template");
        testTemplateDTO.setDescription("Test Description");
        testTemplateDTO.setStatus("PENDING");
        testTemplateDTO.setHrId(testHrId.toString());
        testTemplateDTO.setHrName("HR Manager");
    }

    @Test
    void createTemplate_ValidRequest_ShouldReturnCreated() {
        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setTitle("Onboarding Template");
        request.setDescription("Standard onboarding process");
        request.setHrId(testHrId);
        request.setDepartmentIds(Arrays.asList(1));

        TemplateDTO createdTemplate = new TemplateDTO();
        createdTemplate.setId(1);
        createdTemplate.setTitle("Onboarding Template");
        createdTemplate.setDescription("Standard onboarding process");
        createdTemplate.setStatus("PENDING");

        when(templateService.createTemplate(any(TemplateDTO.class))).thenReturn(createdTemplate);

        ResponseEntity<Result> response = templateController.createTemplate(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Template created successfully", response.getBody().getMessage());
        verify(templateService).createTemplate(any(TemplateDTO.class));
    }

    @Test
    void getTemplateById_ExistingTemplate_ShouldReturnTemplate() {
        Integer templateId = 1;
        when(templateService.getTemplateById(templateId)).thenReturn(testTemplateDTO);

        ResponseEntity<Result> response = templateController.getTemplateById(templateId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Template retrieved successfully", response.getBody().getMessage());
        verify(templateService).getTemplateById(templateId);
    }

    @Test
    void updateTemplate_ValidRequest_ShouldReturnUpdated() {
        Integer templateId = 1;
        UpdateTemplateRequest request = new UpdateTemplateRequest();
        request.setTitle("Updated Template");
        request.setDescription("Updated description");
        request.setStatus("IN_PROGRESS");

        TemplateDTO updatedTemplate = new TemplateDTO();
        updatedTemplate.setId(templateId);
        updatedTemplate.setTitle("Updated Template");
        updatedTemplate.setDescription("Updated description");
        updatedTemplate.setStatus("IN_PROGRESS");

        when(templateService.updateTemplate(eq(templateId), any(TemplateDTO.class))).thenReturn(updatedTemplate);

        ResponseEntity<Result> response = templateController.updateTemplate(templateId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Template updated successfully", response.getBody().getMessage());
        verify(templateService).updateTemplate(eq(templateId), any(TemplateDTO.class));
    }

    @Test
    void deleteTemplate_ExistingTemplate_ShouldReturnSuccess() {
        Integer templateId = 1;
        doNothing().when(templateService).deleteTemplate(templateId);

        ResponseEntity<Result> response = templateController.deleteTemplate(templateId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Template deleted successfully", response.getBody().getMessage());
        verify(templateService).deleteTemplate(templateId);
    }

    @Test
    void assignTemplateToHire_ValidRequest_ShouldReturnSuccess() {
        Integer templateId = 1;
        UUID hireId = UUID.randomUUID();
        doNothing().when(templateService).assignTemplateToHire(templateId, hireId);

        ResponseEntity<Result> response = templateController.assignTemplateToHire(templateId, hireId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Template assigned successfully", response.getBody().getMessage());
        verify(templateService).assignTemplateToHire(templateId, hireId);
    }

    @Test
    void assignTemplateToMultipleHires_ValidRequest_ShouldReturnSuccess() {
        AssignTemplateRequest request = new AssignTemplateRequest();
        request.setTemplateId(1);
        request.setHireIds(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));

        doNothing().when(templateService).assignTemplateToHire(anyInt(), any(UUID.class));

        ResponseEntity<Result> response = templateController.assignTemplateToMultipleHires(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Template assigned to 2 hires successfully", response.getBody().getMessage());
        verify(templateService, times(2)).assignTemplateToHire(anyInt(), any(UUID.class));
    }

    @Test
    void getAllTemplates_ShouldReturnTemplates() {
        List<TemplateDTO> templates = Arrays.asList(testTemplateDTO);
        when(templateService.getAllTemplates()).thenReturn(templates);

        ResponseEntity<Result> response = templateController.getAllTemplates();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Templates retrieved successfully", response.getBody().getMessage());
        verify(templateService).getAllTemplates();
    }
}