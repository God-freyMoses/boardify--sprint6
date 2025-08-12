package com.shaper.server.controller;

import com.shaper.server.model.entity.CompanyDepartment;
import com.shaper.server.model.entity.Hire;
import com.shaper.server.model.entity.Template;
import com.shaper.server.service.DepartmentService;
import com.shaper.server.system.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    
    private final DepartmentService departmentService;
    
    // CRUD Operations
    
    @GetMapping
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getAllDepartments() {
        try {
            List<CompanyDepartment> departments = departmentService.getAllDepartments();
            Result result = new Result(200, true, "Departments retrieved successfully!", departments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving departments: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @GetMapping("/{departmentId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getDepartmentById(@PathVariable Integer departmentId) {
        try {
            CompanyDepartment department = departmentService.getDepartmentById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
            Result result = new Result(200, true, "Department retrieved successfully!", department);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(404, false, "Error retrieving department: " + e.getMessage());
            return ResponseEntity.status(404).body(result);
        }
    }
    
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getDepartmentsByCompany(@PathVariable Integer companyId) {
        try {
            List<CompanyDepartment> departments = departmentService.getDepartmentsByCompany(companyId);
            Result result = new Result(200, true, "Company departments retrieved successfully!", departments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving company departments: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @GetMapping("/hr/{hrUserId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getDepartmentsByHrUser(@PathVariable UUID hrUserId) {
        try {
            List<CompanyDepartment> departments = departmentService.getDepartmentsByHrUser(hrUserId);
            Result result = new Result(200, true, "HR user departments retrieved successfully!", departments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving HR user departments: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> createDepartment(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            Integer companyId = Integer.valueOf(request.get("companyId").toString());
            UUID hrUserId = UUID.fromString(request.get("hrUserId").toString());
            
            CompanyDepartment department = departmentService.createDepartment(name, companyId, hrUserId);
            Result result = new Result(201, true, "Department created successfully!", department);
            return ResponseEntity.status(201).body(result);
        } catch (Exception e) {
            Result result = new Result(400, false, "Error creating department: " + e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }
    
    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> updateDepartment(@PathVariable Integer departmentId, 
                                                 @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            CompanyDepartment department = departmentService.updateDepartment(departmentId, name);
            Result result = new Result(200, true, "Department updated successfully!", department);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(400, false, "Error updating department: " + e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }
    
    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> deleteDepartment(@PathVariable Integer departmentId) {
        try {
            departmentService.deleteDepartment(departmentId);
            Result result = new Result(200, true, "Department deleted successfully!", null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(400, false, "Error deleting department: " + e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }
    
    // Template Association
    
    @PostMapping("/{departmentId}/templates/{templateId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> assignTemplateToD epartment(@PathVariable Integer departmentId,
                                                            @PathVariable Integer templateId) {
        try {
            CompanyDepartment department = departmentService.assignTemplateToD epartment(departmentId, templateId);
            Result result = new Result(200, true, "Template assigned to department successfully!", department);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(400, false, "Error assigning template: " + e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }
    
    @DeleteMapping("/{departmentId}/templates/{templateId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> removeTemplateFromDepartment(@PathVariable Integer departmentId,
                                                             @PathVariable Integer templateId) {
        try {
            CompanyDepartment department = departmentService.removeTemplateFromDepartment(departmentId, templateId);
            Result result = new Result(200, true, "Template removed from department successfully!", department);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(400, false, "Error removing template: " + e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }
    
    @GetMapping("/{departmentId}/templates")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getTemplatesByDepartment(@PathVariable Integer departmentId) {
        try {
            List<Template> templates = departmentService.getTemplatesByDepartment(departmentId);
            Result result = new Result(200, true, "Department templates retrieved successfully!", templates);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving department templates: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @GetMapping("/template/{templateId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getDepartmentsByTemplate(@PathVariable Integer templateId) {
        try {
            List<CompanyDepartment> departments = departmentService.getDepartmentsByTemplate(templateId);
            Result result = new Result(200, true, "Template departments retrieved successfully!", departments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving template departments: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    // Hire Assignment
    
    @PostMapping("/{departmentId}/hires/{hireId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> assignHireToDepartment(@PathVariable Integer departmentId,
                                                       @PathVariable UUID hireId) {
        try {
            departmentService.assignHireToDepartment(hireId, departmentId);
            Result result = new Result(200, true, "Hire assigned to department successfully!", null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(400, false, "Error assigning hire: " + e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }
    
    @DeleteMapping("/hires/{hireId}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> removeHireFromDepartment(@PathVariable UUID hireId) {
        try {
            departmentService.removeHireFromDepartment(hireId);
            Result result = new Result(200, true, "Hire removed from department successfully!", null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(400, false, "Error removing hire: " + e.getMessage());
            return ResponseEntity.status(400).body(result);
        }
    }
    
    @GetMapping("/{departmentId}/hires")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getHiresByDepartment(@PathVariable Integer departmentId) {
        try {
            List<Hire> hires = departmentService.getHiresByDepartment(departmentId);
            Result result = new Result(200, true, "Department hires retrieved successfully!", hires);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving department hires: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    // Analytics and Reporting
    
    @GetMapping("/{departmentId}/analytics")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getDepartmentAnalytics(@PathVariable Integer departmentId) {
        try {
            Map<String, Object> analytics = departmentService.getDepartmentAnalytics(departmentId);
            Result result = new Result(200, true, "Department analytics retrieved successfully!", analytics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving department analytics: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @GetMapping("/company/{companyId}/analytics")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getCompanyDepartmentAnalytics(@PathVariable Integer companyId) {
        try {
            Map<String, Object> analytics = departmentService.getCompanyDepartmentAnalytics(companyId);
            Result result = new Result(200, true, "Company department analytics retrieved successfully!", analytics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving company department analytics: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @GetMapping("/company/{companyId}/progress-summary")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Result> getDepartmentProgressSummary(@PathVariable Integer companyId) {
        try {
            List<Map<String, Object>> summary = departmentService.getDepartmentProgressSummary(companyId);
            Result result = new Result(200, true, "Department progress summary retrieved successfully!", summary);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving department progress summary: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}