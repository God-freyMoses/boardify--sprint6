package com.shaper.server.service.impl;

import com.shaper.server.model.dto.DocumentDto;
import com.shaper.server.model.entity.Document;
import com.shaper.server.model.entity.Task;
import com.shaper.server.repository.DocumentRepository;
import com.shaper.server.repository.TaskRepository;
import com.shaper.server.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    
    private final DocumentRepository documentRepository;
    private final TaskRepository taskRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Override
    @Transactional
    public DocumentDto uploadDocument(MultipartFile file, Integer taskId) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create document entity
            Document document = new Document();
            document.setName(originalFilename != null ? originalFilename : uniqueFilename);
            document.setFilePath(filePath.toString());
            document.setTask(task);
            document.setRequiresSignature(task.isRequiresSignature());
            
            Document savedDocument = documentRepository.save(document);
            return convertToDto(savedDocument, file.getSize(), file.getContentType());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public DocumentDto getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        return convertToDto(document, 0L, null);
    }
    
    @Override
    public List<DocumentDto> getDocumentsByTaskId(Integer taskId) {
        List<Document> documents = documentRepository.findByTaskId(taskId);
        return documents.stream()
            .map(doc -> convertToDto(doc, 0L, null))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DocumentDto> getDocumentsByTemplateId(Integer templateId) {
        List<Document> documents = documentRepository.findByTask_Templates_Id(templateId);
        return documents.stream()
            .map(doc -> convertToDto(doc, 0L, null))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteDocument(Integer id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        
        try {
            // Delete file from disk
            Path filePath = Paths.get(document.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log error but don't fail the operation
            System.err.println("Failed to delete file: " + e.getMessage());
        }
        
        documentRepository.deleteById(id);
    }
    
    @Override
    public byte[] downloadDocument(Integer id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        
        try {
            Path filePath = Paths.get(document.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getDocumentDownloadUrl(Integer id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        return "/api/documents/" + id + "/download";
    }
    
    @Override
    @Transactional
    public DocumentDto uploadDocumentToTemplate(MultipartFile file, Integer templateId) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // For template uploads, we'll associate the document with the first task of the template
        // or create a general template task if no tasks exist
        Task firstTask = taskRepository.findByTemplateId(templateId).stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Template has no tasks to associate document with"));
        
        return uploadDocument(file, firstTask.getId());
    }
    
    private DocumentDto convertToDto(Document document, Long fileSize, String contentType) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setFilePath(document.getFilePath());
        dto.setDownloadUrl(getDocumentDownloadUrl(document.getId()));
        dto.setRequiresSignature(document.isRequiresSignature());
        dto.setTaskId(document.getTask().getId());
        dto.setTaskTitle(document.getTask().getTitle());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setFileSize(fileSize);
        dto.setContentType(contentType);
        return dto;
    }
}