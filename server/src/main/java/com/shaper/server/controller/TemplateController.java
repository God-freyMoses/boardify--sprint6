package com.shaper.server.controller;

import com.shaper.server.model.dto.TemplateDTO;
import com.shaper.server.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TemplateController {
    
    private final TemplateService templateService;
    
    @GetMapping
    public ResponseEntity<List<TemplateDTO>> getAllTemplates() {
        try {
            List<TemplateDTO> templates = templateService.getAllTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<TemplateDTO> createTemplate(@RequestBody TemplateDTO templateDto) {
        try {
            TemplateDTO createdTemplate = templateService.createTemplate(templateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTemplate);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TemplateDTO> updateTemplate(@PathVariable Integer id, @RequestBody TemplateDTO templateDto) {
        try {
            TemplateDTO updatedTemplate = templateService.updateTemplate(id, templateDto);
            return ResponseEntity.ok(updatedTemplate);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDTO> getTemplateById(@PathVariable Integer id) {
        try {
            TemplateDTO template = templateService.getTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<TemplateDTO>> getTemplatesByDepartmentId(@PathVariable Integer departmentId) {
        try {
            List<TemplateDTO> templates = templateService.getTemplatesByDepartmentId(departmentId);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hr/{hrId}")
    public ResponseEntity<List<TemplateDTO>> getTemplatesByHrId(@PathVariable UUID hrId) {
        try {
            List<TemplateDTO> templates = templateService.getTemplatesByHrId(hrId);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<TemplateDTO>> getTemplatesByCompanyId(@PathVariable Integer companyId) {
        try {
            List<TemplateDTO> templates = templateService.getTemplatesByCompanyId(companyId);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Integer id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @PostMapping("/{templateId}/assign/{hireId}")
    public ResponseEntity<Void> assignTemplateToHire(@PathVariable Integer templateId, @PathVariable UUID hireId) {
        try {
            templateService.assignTemplateToHire(templateId, hireId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}