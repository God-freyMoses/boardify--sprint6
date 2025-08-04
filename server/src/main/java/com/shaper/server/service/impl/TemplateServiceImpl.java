package com.shaper.server.service.impl;

import com.shaper.server.model.dto.TemplateDTO;
import com.shaper.server.model.entity.*;
import com.shaper.server.repository.*;
import com.shaper.server.service.TemplateService;
import com.shaper.server.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    
    private final TemplateRepository templateRepository;
    private final HrUserRepository hrUserRepository;
    private final CompanyDepartmentRepository departmentRepository;
    private final HireRepository hireRepository;
    private final TodoService todoService;
    
    @Override
    @Transactional
    public TemplateDTO createTemplate(TemplateDTO templateDto) {
        Template template = new Template();
        template.setTitle(templateDto.getTitle());
        template.setDescription(templateDto.getDescription());
        
        // Set HR user
        HrUser hrUser = hrUserRepository.findById(UUID.fromString(templateDto.getHrId()))
            .orElseThrow(() -> new RuntimeException("HR User not found"));
        template.setCreatedByHr(hrUser);
        
        // Set departments
        if (templateDto.getDepartmentIds() != null && !templateDto.getDepartmentIds().isEmpty()) {
            List<CompanyDepartment> departments = departmentRepository.findAllById(templateDto.getDepartmentIds());
            template.setDepartments(departments.stream().collect(Collectors.toSet()));
        }
        
        Template savedTemplate = templateRepository.save(template);
        return convertToDto(savedTemplate);
    }
    
    @Override
    @Transactional
    public TemplateDTO updateTemplate(Integer id, TemplateDTO templateDto) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template not found"));
        
        template.setTitle(templateDto.getTitle());
        template.setDescription(templateDto.getDescription());
        if (templateDto.getStatus() != null) {
            template.setStatus(Template.TemplateStatus.valueOf(templateDto.getStatus()));
        }
        
        // Update departments if provided
        if (templateDto.getDepartmentIds() != null) {
            List<CompanyDepartment> departments = departmentRepository.findAllById(templateDto.getDepartmentIds());
            template.setDepartments(departments.stream().collect(Collectors.toSet()));
        }
        
        Template savedTemplate = templateRepository.save(template);
        return convertToDto(savedTemplate);
    }
    
    @Override
    public TemplateDTO getTemplateById(Integer id) {
        Template template = templateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template not found"));
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
        templateRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void assignTemplateToHire(Integer templateId, UUID hireId) {
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));
        
        Hire hire = hireRepository.findById(hireId)
            .orElseThrow(() -> new RuntimeException("Hire not found"));
        
        // Create todos from template tasks
        todoService.createTodosFromTemplate(templateId, hireId);
        
        // Update template status to IN_PROGRESS if it was PENDING
        if (template.getStatus() == Template.TemplateStatus.PENDING) {
            template.setStatus(Template.TemplateStatus.IN_PROGRESS);
            templateRepository.save(template);
        }
    }
    
    @Override
    public Template getTemplateEntityById(Integer id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template not found"));
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