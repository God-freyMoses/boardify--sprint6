package com.shaper.server.controller;

import com.shaper.server.model.dto.DocumentDto;
import com.shaper.server.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping("/upload")
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "taskId", required = false) Integer taskId,
            @RequestParam(value = "templateId", required = false) Integer templateId) {
        System.out.println("=== DOCUMENT CONTROLLER UPLOAD ENDPOINT CALLED ===");
        System.out.println("File: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("TaskId: " + taskId);
        System.out.println("TemplateId: " + templateId);
        
        try {
            DocumentDto uploadedDocument;
            if (taskId != null) {
                System.out.println("Uploading to task: " + taskId);
                uploadedDocument = documentService.uploadDocument(file, taskId);
            } else if (templateId != null) {
                System.out.println("Uploading to template: " + templateId);
                uploadedDocument = documentService.uploadDocumentToTemplate(file, templateId);
            } else {
                System.out.println("ERROR: Neither taskId nor templateId provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            System.out.println("Upload successful, returning document: " + uploadedDocument.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocument);
        } catch (Exception e) {
            System.out.println("ERROR in DocumentController: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable Integer id) {
        try {
            DocumentDto document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByTaskId(@PathVariable Integer taskId) {
        try {
            List<DocumentDto> documents = documentService.getDocumentsByTaskId(taskId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<DocumentDto>> getDocumentsByTemplateId(@PathVariable Integer templateId) {
        try {
            List<DocumentDto> documents = documentService.getDocumentsByTemplateId(templateId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Integer id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer id) {
        try {
            byte[] documentData = documentService.downloadDocument(id);
            DocumentDto document = documentService.getDocumentById(id);
            
            ByteArrayResource resource = new ByteArrayResource(documentData);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/{id}/url")
    public ResponseEntity<String> getDocumentDownloadUrl(@PathVariable Integer id) {
        try {
            String downloadUrl = documentService.getDocumentDownloadUrl(id);
            return ResponseEntity.ok(downloadUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}