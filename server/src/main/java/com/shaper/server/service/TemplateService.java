package com.shaper.server.service;

import com.shaper.server.model.dto.TemplateDTO;
import com.shaper.server.model.entity.Template;

import java.util.List;
import java.util.UUID;

public interface TemplateService {
    
    TemplateDTO createTemplate(TemplateDTO templateDto);
    
    TemplateDTO updateTemplate(Integer id, TemplateDTO templateDto);
    
    TemplateDTO getTemplateById(Integer id);
    
    List<TemplateDTO> getAllTemplates();
    
    List<TemplateDTO> getTemplatesByDepartmentId(Integer departmentId);
    
    List<TemplateDTO> getTemplatesByHrId(UUID hrId);
    
    List<TemplateDTO> getTemplatesByCompanyId(Integer companyId);
    
    void deleteTemplate(Integer id);
    
    void assignTemplateToHire(Integer templateId, UUID hireId);
    
    Template getTemplateEntityById(Integer id);
}