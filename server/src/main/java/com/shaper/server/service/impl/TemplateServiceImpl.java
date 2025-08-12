package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.dto.TemplateDTO;
import com.shaper.server.model.dto.TodoDto;
import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.TemplateStatus;
import com.shaper.server.repository.*;
import com.shaper.server.service.ProgressService;
import com.shaper.server.service.TemplateService;
import com.shaper.server.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceImpl implements TemplateService {
    
    private final TemplateRepository templateRepository;
    private final HrUserRepository hrUserRepository;
    private final CompanyDepartmentRepository departmentRepository;
    private final HireRepository hireRepository;
    private final TodoRepository todoRepository;
    private final TodoService todoService;
    private final ProgressService progressService;
    
    @Override
    @Transactional
    public TemplateDTO createTemplate(TemplateDTO templateDto) {
        log.debug("Creating template with title: {}", templateDto.getTitle());
        
        // Validate required fields
        if (templateDto.getTitle() == null || templateDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Template title is required");
        }
        if (templateDto.getHrId() == null || templateDto.getHrId().trim().isEmpty()) {
            throw new IllegalArgumentException("HR user ID is required");
        }
        
        Template template = new Template();
        template.setTitle(templateDto.getTitle().trim());
        template.setDescription(templateDto.getDescription() != null ? templateDto.getDescription().trim() : null);
        template.setStatus(TemplateStatus.PENDING);
        
        // Set HR user
        HrUser hrUser = hrUserRepository.findById(UUID.fromString(templateDto.getHrId()))
            .orElseThrow(() -> new DataNotFoundException("HR User not found with ID: " + templateDto.getHrId()));
        template.setCreatedByHr(hrUser);
        
        // Set departments
        if (templateDto.getDepartmentIds() != null && !templateDto.getDepartmentIds().isEmpty()) {
            List<CompanyDepartment> departments = departmentRepository.findAllById(templateDto.getDepartmentIds());
            if (departments.size() != templateDto.getDepartmentIds().size()) {
                throw new DataNotFoundException("One or more departments not found");
            }
            template.setDepartments(departments.stream().collect(Collectors.toSet()));
        }
        
        Template savedTemplate = templateRepository.save(template);
        log.debug("Created template with ID: {}", savedTemplate.getId());
        
        return convertToDto(savedTemplate);
    }
    
    @Override
    @Transactional
    public TemplateDTO updateTemplate(Integer id, TemplateDTO templateDto) {
        log.debug("Updating template with ID: {}", id);
        
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Template not found with ID: " + id));
        
        // Validate required fields
        if (templateDto.getTitle() == null || templateDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Template title is required");
        }
        
        template.setTitle(templateDto.getTitle().trim());
        template.setDescription(templateDto.getDescription() != null ? templateDto.getDescription().trim() : null);
        
        if (templateDto.getStatus() != null && !templateDto.getStatus().trim().isEmpty()) {
            try {
                template.setStatus(TemplateStatus.valueOf(templateDto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid template status: " + templateDto.getStatus());
            }
        }
        
        // Update departments if provided
        if (templateDto.getDepartmentIds() != null) {
            if (templateDto.getDepartmentIds().isEmpty()) {
                template.setDepartments(null);
            } else {
                List<CompanyDepartment> departments = departmentRepository.findAllById(templateDto.getDepartmentIds());
                if (departments.size() != templateDto.getDepartmentIds().size()) {
                    throw new DataNotFoundException("One or more departments not found");
                }
                template.setDepartments(departments.stream().collect(Collectors.toSet()));
            }
        }
        
        Template savedTemplate = templateRepository.save(template);
        log.debug("Updated template with ID: {}", id);
        
        return convertToDto(savedTemplate);
    }
    
    @Override
    public TemplateDTO getTemplateById(Integer id) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Template not found with ID: " + id));
        return convertToDto(template);
    }
    
    @Override
    public List<TemplateDTO> getAllTemplates() {
        List<Template> templates = templateRepository.findAll();
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public List<TemplateDTO> getTemplatesByDepartmentId(Integer departmentId) {
        List<Template> templates = templateRepository.findByDepartmentId(departmentId);
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public List<TemplateDTO> getTemplatesByHrId(UUID hrId) {
        List<Template> templates = templateRepository.findByCreatedByHr_Id(hrId);
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public List<TemplateDTO> getTemplatesByCompanyId(Integer companyId) {
        List<Template> templates = templateRepository.findByCompanyId(companyId);
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteTemplate(Integer id) {
        log.debug("Deleting template with ID: {}", id);
        
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Template not found with ID: " + id));
        
        // Check if template has active assignments (todos)
        List<Todo> activeTodos = todoRepository.findByTemplate_Id(id);
        if (!activeTodos.isEmpty()) {
            throw new IllegalStateException("Cannot delete template with active assignments. " +
                "Template has " + activeTodos.size() + " active todos.");
        }
        
        templateRepository.deleteById(id);
        log.debug("Deleted template with ID: {}", id);
    }
    
    @Override
    @Transactional
    public void assignTemplateToHire(Integer templateId, UUID hireId) {
        log.debug("Assigning template ID: {} to hire ID: {}", templateId, hireId);
        
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new DataNotFoundException("Template not found with ID: " + templateId));
        
        Hire hire = hireRepository.findById(hireId)
            .orElseThrow(() -> new DataNotFoundException("Hire not found with ID: " + hireId));
        
        // Validate that the template has tasks
        if (template.getTasks() == null || template.getTasks().isEmpty()) {
            throw new IllegalStateException("Cannot assign template without tasks. Template ID: " + templateId);
        }
        
        // Check if template is already assigned to this hire
        List<Todo> existingTodos = todoRepository.findByHireIdAndTemplateId(hireId, templateId);
        if (!existingTodos.isEmpty()) {
            log.warn("Template ID: {} is already assigned to hire ID: {}", templateId, hireId);
            return;
        }
        
        // Create todos from template tasks
        List<TodoDto> createdTodos = todoService.createTodosFromTemplate(templateId, hireId);
        log.debug("Created {} todos for hire ID: {} from template ID: {}", 
                 createdTodos.size(), hireId, templateId);
        
        // Initialize progress tracking
        progressService.initializeProgress(hireId, templateId);
        log.debug("Initialized progress tracking for hire ID: {} and template ID: {}", hireId, templateId);
        
        // Update template status to IN_PROGRESS if it was PENDING
        if (template.getStatus() == TemplateStatus.PENDING) {
            template.setStatus(TemplateStatus.IN_PROGRESS);
            templateRepository.save(template);
            log.debug("Updated template ID: {} status to IN_PROGRESS", templateId);
        }
        
        log.debug("Successfully assigned template ID: {} to hire ID: {}", templateId, hireId);
    }
    
    @Override
    public Template getTemplateEntityById(Integer id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Template not found with ID: " + id));
    }
    
    private TemplateDTO convertToDto(Template template) {
        TemplateDTO dto = new TemplateDTO();
        dto.setId(template.getId());
        dto.setTitle(template.getTitle());
        dto.setDescription(template.getDescription());
        dto.setStatus(template.getStatus().name());
        dto.setHrId(template.getCreatedByHr().getId().toString());
        dto.setHrName(template.getCreatedByHr().getFirstName() + " " + template.getCreatedByHr().getLastName());
        dto.setCreatedDate(template.getCreatedDate());
        dto.setUpdatedDate(template.getUpdatedDate());
        
        if (template.getTasks() != null) {
            dto.setTaskIds(template.getTasks().stream()
                .map(Task::getId)
                .collect(Collectors.toList()));
        }
        
        if (template.getDepartments() != null) {
            dto.setDepartmentIds(template.getDepartments().stream()
                .map(CompanyDepartment::getId)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
}