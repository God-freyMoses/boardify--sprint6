package com.shaper.server.controller;

import com.shaper.server.model.entity.Company;
import com.shaper.server.model.entity.CompanyDepartment;
import com.shaper.server.model.entity.HrUser;
import com.shaper.server.repository.CompanyDepartmentRepository;
import com.shaper.server.repository.CompanyRepository;
import com.shaper.server.repository.HrUserRepository;
import com.shaper.server.system.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final CompanyDepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final HrUserRepository hrUserRepository;

    public DepartmentController(CompanyDepartmentRepository departmentRepository, 
                              CompanyRepository companyRepository,
                              HrUserRepository hrUserRepository) {
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.hrUserRepository = hrUserRepository;
    }

    @GetMapping
    public ResponseEntity<Result> getAllDepartments() {
        try {
            List<CompanyDepartment> departments = departmentRepository.findAll();
            // Create a simplified list to avoid circular reference issues
            List<Map<String, Object>> simplifiedDepartments = departments.stream()
                .map(dept -> {
                    Map<String, Object> deptMap = new java.util.HashMap<>();
                    deptMap.put("id", dept.getId());
                    deptMap.put("name", dept.getName());
                    deptMap.put("companyId", dept.getCompany().getId());
                    deptMap.put("companyName", dept.getCompany().getName());
                    return deptMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Result result = new Result(200, true, "Departments retrieved successfully!", simplifiedDepartments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving departments: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Result> createDepartment(@RequestBody Map<String, Object> request) {
        try {
            String departmentName = (String) request.get("name");
            Integer companyId = Integer.valueOf(request.get("companyId").toString());
            
            // Find company
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            
            // Find HR user for this company
            HrUser hrUser = hrUserRepository.findByCompanyId(companyId);
            if (hrUser == null) {
                throw new RuntimeException("No HR user found for company");
            }
            
            // Create department
            CompanyDepartment department = new CompanyDepartment();
            department.setName(departmentName);
            department.setCompany(company);
            department.setCreatedByHr(hrUser);
            
            CompanyDepartment savedDepartment = departmentRepository.save(department);
            
            Result result = new Result(200, true, "Department created successfully!", savedDepartment.getId());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Result result = new Result(500, false, "Error creating department: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}