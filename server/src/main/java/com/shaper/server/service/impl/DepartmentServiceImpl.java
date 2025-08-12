package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.entity.*;
import com.shaper.server.repository.*;
import com.shaper.server.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    
    private final CompanyDepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final HrUserRepository hrUserRepository;
    private final TemplateRepository templateRepository;
    private final HireRepository hireRepository;
    private final ProgressRepository progressRepository;
    
    @Override
    @Transactional
    public CompanyDepartment createDepartment(String name, Integer companyId, UUID hrUserId) {
        log.info("Creating department '{}' for company {} by HR user {}", name, companyId, hrUserId);
        
        // Validate company exists
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new DataNotFoundException("Company not found with id: " + companyId));
        
        // Validate HR user exists and belongs to company
        HrUser hrUser = hrUserRepository.findById(hrUserId)
            .orElseThrow(() -> new DataNotFoundException("HR user not found with id: " + hrUserId));
        
        if (!hrUser.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("HR user does not belong to the specified company");
        }
        
        // Check if department name already exists in company
        if (departmentExistsInCompany(name, companyId)) {
            throw new IllegalArgumentException("Department with name '" + name + "' already exists in this company");
        }
        
        CompanyDepartment department = new CompanyDepartment();
        department.setName(name);
        department.setCompany(company);
        department.setCreatedByHr(hrUser);
        department.setAssignedTemplates(new HashSet<>());
        department.setHires(new HashSet<>());
        
        CompanyDepartment savedDepartment = departmentRepository.save(department);
        log.info("Department created successfully with id: {}", savedDepartment.getId());
        
        return savedDepartment;
    }
    
    @Override
    public Optional<CompanyDepartment> getDepartmentById(Integer departmentId) {
        return departmentRepository.findById(departmentId);
    }
    
    @Override
    public List<CompanyDepartment> getAllDepartments() {
        return departmentRepository.findAll();
    }
    
    @Override
    public List<CompanyDepartment> getDepartmentsByCompany(Integer companyId) {
        return departmentRepository.findByCompany_Id(companyId);
    }
    
    @Override
    public List<CompanyDepartment> getDepartmentsByHrUser(UUID hrUserId) {
        return departmentRepository.findByCreatedByHr_Id(hrUserId);
    }
    
    @Override
    @Transactional
    public CompanyDepartment updateDepartment(Integer departmentId, String name) {
        log.info("Updating department {} with new name: {}", departmentId, name);
        
        CompanyDepartment department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + departmentId));
        
        // Check if new name conflicts with existing departments in the same company
        if (!department.getName().equals(name) && 
            departmentExistsInCompany(name, department.getCompany().getId())) {
            throw new IllegalArgumentException("Department with name '" + name + "' already exists in this company");
        }
        
        department.setName(name);
        CompanyDepartment updatedDepartment = departmentRepository.save(department);
        
        log.info("Department updated successfully");
        return updatedDepartment;
    }
    
    @Override
    @Transactional
    public void deleteDepartment(Integer departmentId) {
        log.info("Deleting department with id: {}", departmentId);
        
        CompanyDepartment department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + departmentId));
        
        // Check if department has active hires
        if (!isDepartmentEmpty(departmentId)) {
            throw new IllegalStateException("Cannot delete department with active hires. Please reassign hires first.");
        }
        
        // Remove template associations
        department.getAssignedTemplates().clear();
        departmentRepository.save(department);
        
        // Delete department
        departmentRepository.delete(department);
        log.info("Department deleted successfully");
    }
    
    @Override
    @Transactional
    public CompanyDepartment assignTemplateToD epartment(Integer departmentId, Integer templateId) {
        log.info("Assigning template {} to department {}", templateId, departmentId);
        
        CompanyDepartment department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + departmentId));
        
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new DataNotFoundException("Template not found with id: " + templateId));
        
        // Validate template belongs to same company
        if (!template.getCompany().getId().equals(department.getCompany().getId())) {
            throw new IllegalArgumentException("Template does not belong to the same company as the department");
        }
        
        department.getAssignedTemplates().add(template);
        CompanyDepartment updatedDepartment = departmentRepository.save(department);
        
        log.info("Template assigned to department successfully");
        return updatedDepartment;
    }
    
    @Override
    @Transactional
    public CompanyDepartment removeTemplateFromDepartment(Integer departmentId, Integer templateId) {
        log.info("Removing template {} from department {}", templateId, departmentId);
        
        CompanyDepartment department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + departmentId));
        
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new DataNotFoundException("Template not found with id: " + templateId));
        
        department.getAssignedTemplates().remove(template);
        CompanyDepartment updatedDepartment = departmentRepository.save(department);
        
        log.info("Template removed from department successfully");
        return updatedDepartment;
    }
    
    @Override
    public List<Template> getTemplatesByDepartment(Integer departmentId) {
        CompanyDepartment department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + departmentId));
        
        return new ArrayList<>(department.getAssignedTemplates());
    }
    
    @Override
    public List<CompanyDepartment> getDepartmentsByTemplate(Integer templateId) {
        return departmentRepository.findByTemplateId(templateId);
    }
    
    @Override
    @Transactional
    public void assignHireToDepartment(UUID hireId, Integer departmentId) {
        log.info("Assigning hire {} to department {}", hireId, departmentId);
        
        Hire hire = hireRepository.findById(hireId)
            .orElseThrow(() -> new DataNotFoundException("Hire not found with id: " + hireId));
        
        CompanyDepartment department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + departmentId));
        
        // Validate hire belongs to same company
        if (!hire.getCompany().getId().equals(department.getCompany().getId())) {
            throw new IllegalArgumentException("Hire does not belong to the same company as the department");
        }
        
        hire.setDepartment(department);
        hireRepository.save(hire);
        
        log.info("Hire assigned to department successfully");
    }
    
    @Override
    @Transactional
    public void removeHireFromDepartment(UUID hireId) {
        log.info("Removing hire {} from department", hireId);
        
        Hire hire = hireRepository.findById(hireId)
            .orElseThrow(() -> new DataNotFoundException("Hire not found with id: " + hireId));
        
        hire.setDepartment(null);
        hireRepository.save(hire);
        
        log.info("Hire removed from department successfully");
    }
    
    @Override
    public List<Hire> getHiresByDepartment(Integer departmentId) {
        return hireRepository.findByDepartment_Id(departmentId);
    }
    
    @Override
    public Map<String, Object> getDepartmentAnalytics(Integer departmentId) {
        CompanyDepartment department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + departmentId));
        
        List<Hire> hires = getHiresByDepartment(departmentId);
        List<Progress> departmentProgress = progressRepository.findByDepartmentId(departmentId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("departmentId", departmentId);
        analytics.put("departmentName", department.getName());
        analytics.put("totalHires", hires.size());
        analytics.put("activeHires", getActiveHiresByDepartment(departmentId));
        analytics.put("assignedTemplates", department.getAssignedTemplates().size());
        analytics.put("averageCompletion", getAverageCompletionByDepartment(departmentId));
        
        // Calculate completion distribution
        Map<String, Long> completionDistribution = departmentProgress.stream()
            .collect(Collectors.groupingBy(
                progress -> {
                    double completion = progress.getCompletionPercentage();
                    if (completion == 100.0) return "completed";
                    else if (completion >= 75.0) return "near_completion";
                    else if (completion >= 50.0) return "in_progress";
                    else if (completion >= 25.0) return "started";
                    else return "not_started";
                },
                Collectors.counting()
            ));
        
        analytics.put("completionDistribution", completionDistribution);
        
        return analytics;
    }
    
    @Override
    public Map<String, Object> getCompanyDepartmentAnalytics(Integer companyId) {
        List<CompanyDepartment> departments = getDepartmentsByCompany(companyId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("companyId", companyId);
        analytics.put("totalDepartments", departments.size());
        
        long totalHires = departments.stream()
            .mapToLong(dept -> getTotalHiresByDepartment(dept.getId()))
            .sum();
        
        long totalActiveHires = departments.stream()
            .mapToLong(dept -> getActiveHiresByDepartment(dept.getId()))
            .sum();
        
        double overallAverageCompletion = departments.stream()
            .mapToDouble(dept -> getAverageCompletionByDepartment(dept.getId()))
            .average()
            .orElse(0.0);
        
        analytics.put("totalHires", totalHires);
        analytics.put("totalActiveHires", totalActiveHires);
        analytics.put("overallAverageCompletion", overallAverageCompletion);
        
        // Department breakdown
        List<Map<String, Object>> departmentBreakdown = departments.stream()
            .map(dept -> {
                Map<String, Object> deptData = new HashMap<>();
                deptData.put("departmentId", dept.getId());
                deptData.put("departmentName", dept.getName());
                deptData.put("hireCount", getTotalHiresByDepartment(dept.getId()));
                deptData.put("averageCompletion", getAverageCompletionByDepartment(dept.getId()));
                return deptData;
            })
            .collect(Collectors.toList());
        
        analytics.put("departmentBreakdown", departmentBreakdown);
        
        return analytics;
    }
    
    @Override
    public long getTotalHiresByDepartment(Integer departmentId) {
        return hireRepository.countByDepartmentId(departmentId);
    }
    
    @Override
    public long getActiveHiresByDepartment(Integer departmentId) {
        // Assuming active hires are those with incomplete onboarding
        List<Progress> departmentProgress = progressRepository.findByDepartmentId(departmentId);
        return departmentProgress.stream()
            .filter(progress -> progress.getCompletionPercentage() < 100.0)
            .count();
    }
    
    @Override
    public double getAverageCompletionByDepartment(Integer departmentId) {
        List<Progress> departmentProgress = progressRepository.findByDepartmentId(departmentId);
        
        return departmentProgress.stream()
            .mapToDouble(Progress::getCompletionPercentage)
            .average()
            .orElse(0.0);
    }
    
    @Override
    public List<Map<String, Object>> getDepartmentProgressSummary(Integer companyId) {
        List<CompanyDepartment> departments = getDepartmentsByCompany(companyId);
        
        return departments.stream()
            .map(dept -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("departmentId", dept.getId());
                summary.put("departmentName", dept.getName());
                summary.put("totalHires", getTotalHiresByDepartment(dept.getId()));
                summary.put("activeHires", getActiveHiresByDepartment(dept.getId()));
                summary.put("averageCompletion", getAverageCompletionByDepartment(dept.getId()));
                
                List<Progress> progress = progressRepository.findByDepartmentId(dept.getId());
                long completedCount = progress.stream()
                    .filter(p -> p.getCompletionPercentage() == 100.0)
                    .count();
                
                summary.put("completedHires", completedCount);
                summary.put("completionRate", progress.isEmpty() ? 0.0 : 
                    (double) completedCount / progress.size() * 100.0);
                
                return summary;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean departmentExistsInCompany(String name, Integer companyId) {
        return departmentRepository.existsByNameAndCompany_Id(name, companyId);
    }
    
    @Override
    public boolean isDepartmentEmpty(Integer departmentId) {
        return getTotalHiresByDepartment(departmentId) == 0;
    }

    @Override
    public CompanyDepartment assignTemplateToDepartment(Integer departmentId, Integer templateId) {
        CompanyDepartment department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + departmentId));
        
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new DataNotFoundException("Template not found with id: " + templateId));
        
        department.getAssignedTemplates().add(template);
        departmentRepository.save(department);
        
        return department;
    }
}