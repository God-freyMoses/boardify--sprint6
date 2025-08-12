package com.shaper.server.controller;

import com.shaper.server.model.dto.DocumentDto;
import com.shaper.server.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping("/upload")
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "taskId", required = false) Integer taskId,
            @RequestParam(value = "todoId", required = false) Integer todoId,
            @RequestParam(value = "templateId", required = false) Integer templateId) {
        
        log.info("Document upload request - File: {}, TaskId: {}, TodoId: {}, TemplateId: {}", 
                file.getOriginalFilename(), taskId, todoId, templateId);
        
        try {
            DocumentDto uploadedDocument;
            
            if (taskId != null) {
                uploadedDocument = documentService.uploadDocument(file, taskId);
            } else if (todoId != null) {
                uploadedDocument = documentService.uploadDocumentToTodo(file, todoId);
            } else if (templateId != null) {
                uploadedDocument = documentService.uploadDocumentToTemplate(file, templateId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocument);
            
        } catch (Exception e) {
            log.error("Document upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable Integer id) {
        try {
            DocumentDto document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            log.error("Failed to get document by ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByTaskId(@PathVariable Integer taskId) {
        try {
            List<DocumentDto> documents = documentService.getDocumentsByTaskId(taskId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Failed to get documents by task ID {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/todo/{todoId}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByTodoId(@PathVariable Integer todoId) {
        try {
            List<DocumentDto> documents = documentService.getDocumentsByTodoId(todoId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Failed to get documents by todo ID {}: {}", todoId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByTemplateId(@PathVariable Integer templateId) {
        try {
            List<DocumentDto> documents = documentService.getDocumentsByTemplateId(templateId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Failed to get documents by template ID {}: {}", templateId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hire/{hireId}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByHireId(@PathVariable UUID hireId) {
        try {
            List<DocumentDto> documents = documentService.getDocumentsByHireId(hireId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Failed to get documents by hire ID {}: {}", hireId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Integer id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete document with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Integer id,
            @RequestParam(value = "userId", required = false) UUID userId) {
        try {
            byte[] documentData;
            if (userId != null) {
                documentData = documentService.downloadDocument(id, userId);
            } else {
                documentData = documentService.downloadDocument(id);
            }
            
            DocumentDto document = documentService.getDocumentById(id);
            ByteArrayResource resource = new ByteArrayResource(documentData);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Failed to download document with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/{id}/url")
    public ResponseEntity<String> getDocumentDownloadUrl(@PathVariable Integer id) {
        try {
            String downloadUrl = documentService.getDocumentDownloadUrl(id);
            return ResponseEntity.ok(downloadUrl);
        } catch (Exception e) {
            log.error("Failed to get download URL for document with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}