package com.shaper.server.service;

import com.shaper.server.model.entity.CompanyDepartment;
import com.shaper.server.model.entity.Hire;
import com.shaper.server.model.entity.Template;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentService {
    
    // CRUD Operations
    CompanyDepartment createDepartment(String name, Integer companyId, UUID hrUserId);
    Optional<CompanyDepartment> getDepartmentById(Integer departmentId);
    List<CompanyDepartment> getAllDepartments();
    List<CompanyDepartment> getDepartmentsByCompany(Integer companyId);
    List<CompanyDepartment> getDepartmentsByHrUser(UUID hrUserId);
    CompanyDepartment updateDepartment(Integer departmentId, String name);
    void deleteDepartment(Integer departmentId);
    
    // Department-Template Association
    CompanyDepartment assignTemplateToDepartment(Integer departmentId, Integer templateId);
    CompanyDepartment removeTemplateFromDepartment(Integer departmentId, Integer templateId);
    List<Template> getTemplatesByDepartment(Integer departmentId);
    List<CompanyDepartment> getDepartmentsByTemplate(Integer templateId);
    
    // Hire Assignment
    void assignHireToDepartment(UUID hireId, Integer departmentId);
    void removeHireFromDepartment(UUID hireId);
    List<Hire> getHiresByDepartment(Integer departmentId);
    
    // Analytics and Reporting
    Map<String, Object> getDepartmentAnalytics(Integer departmentId);
    Map<String, Object> getCompanyDepartmentAnalytics(Integer companyId);
    long getTotalHiresByDepartment(Integer departmentId);
    long getActiveHiresByDepartment(Integer departmentId);
    double getAverageCompletionByDepartment(Integer departmentId);
    List<Map<String, Object>> getDepartmentProgressSummary(Integer companyId);
    
    // Validation
    boolean departmentExistsInCompany(String name, Integer companyId);
    boolean isDepartmentEmpty(Integer departmentId);
}