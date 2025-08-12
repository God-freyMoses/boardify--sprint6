package com.shaper.server.controller;

import com.shaper.server.model.dto.*;
import com.shaper.server.service.TemplateService;
import com.shaper.server.system.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class TemplateController {
    
    private final TemplateService templateService;
    
    /**
     * Get all templates
     */
    @GetMapping
    public ResponseEntity<Result> getAllTemplates() {
        log.debug("Getting all templates");
        List<TemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Templates retrieved successfully", templates));
    }
    
    /**
     * Create a new template
     */
    @PostMapping
    public ResponseEntity<Result> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        log.debug("Creating template with title: {}", request.getTitle());
        
        TemplateDTO templateDto = new TemplateDTO();
        templateDto.setTitle(request.getTitle());
        templateDto.setDescription(request.getDescription());
        templateDto.setHrId(request.getHrId().toString());
        templateDto.setDepartmentIds(request.getDepartmentIds());
        
        TemplateDTO createdTemplate = templateService.createTemplate(templateDto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new Result(HttpStatus.CREATED.value(), true, "Template created successfully", createdTemplate));
    }
    
    /**
     * Update an existing template
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result> updateTemplate(@PathVariable Integer id, @Valid @RequestBody UpdateTemplateRequest request) {
        log.debug("Updating template with ID: {}", id);
        
        TemplateDTO templateDto = new TemplateDTO();
        templateDto.setTitle(request.getTitle());
        templateDto.setDescription(request.getDescription());
        templateDto.setStatus(request.getStatus());
        templateDto.setDepartmentIds(request.getDepartmentIds());
        
        TemplateDTO updatedTemplate = templateService.updateTemplate(id, templateDto);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Template updated successfully", updatedTemplate));
    }
    
    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result> getTemplateById(@PathVariable Integer id) {
        log.debug("Getting template with ID: {}", id);
        TemplateDTO template = templateService.getTemplateById(id);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Template retrieved successfully", template));
    }
    
    /**
     * Get templates by department ID
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<Result> getTemplatesByDepartmentId(@PathVariable Integer departmentId) {
        log.debug("Getting templates for department ID: {}", departmentId);
        List<TemplateDTO> templates = templateService.getTemplatesByDepartmentId(departmentId);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Templates retrieved successfully", templates));
    }
    
    /**
     * Get templates by HR user ID
     */
    @GetMapping("/hr/{hrId}")
    public ResponseEntity<Result> getTemplatesByHrId(@PathVariable UUID hrId) {
        log.debug("Getting templates for HR ID: {}", hrId);
        List<TemplateDTO> templates = templateService.getTemplatesByHrId(hrId);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Templates retrieved successfully", templates));
    }
    
    /**
     * Get templates by company ID
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<Result> getTemplatesByCompanyId(@PathVariable Integer companyId) {
        log.debug("Getting templates for company ID: {}", companyId);
        List<TemplateDTO> templates = templateService.getTemplatesByCompanyId(companyId);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Templates retrieved successfully", templates));
    }
    
    /**
     * Delete a template
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteTemplate(@PathVariable Integer id) {
        log.debug("Deleting template with ID: {}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Template deleted successfully"));
    }
    
    /**
     * Assign template to a single hire
     */
    @PostMapping("/{templateId}/assign/{hireId}")
    public ResponseEntity<Result> assignTemplateToHire(@PathVariable Integer templateId, @PathVariable UUID hireId) {
        log.debug("Assigning template ID: {} to hire ID: {}", templateId, hireId);
        templateService.assignTemplateToHire(templateId, hireId);
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, "Template assigned successfully"));
    }
    
    /**
     * Assign template to multiple hires
     */
    @PostMapping("/assign")
    public ResponseEntity<Result> assignTemplateToMultipleHires(@Valid @RequestBody AssignTemplateRequest request) {
        log.debug("Assigning template ID: {} to {} hires", request.getTemplateId(), request.getHireIds().size());
        
        for (UUID hireId : request.getHireIds()) {
            templateService.assignTemplateToHire(request.getTemplateId(), hireId);
        }
        
        return ResponseEntity.ok(new Result(HttpStatus.OK.value(), true, 
            "Template assigned to " + request.getHireIds().size() + " hires successfully"));
    }
}