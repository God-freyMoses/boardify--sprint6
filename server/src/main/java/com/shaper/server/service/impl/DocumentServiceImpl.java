package com.shaper.server.service.impl;

import com.shaper.server.model.dto.DocumentDto;
import com.shaper.server.model.entity.Document;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.entity.Todo;
import com.shaper.server.repository.DocumentRepository;
import com.shaper.server.repository.TaskRepository;
import com.shaper.server.repository.TodoRepository;
import com.shaper.server.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    
    private final DocumentRepository documentRepository;
    private final TaskRepository taskRepository;
    private final TodoRepository todoRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.upload.max-file-size:10485760}") // 10MB default
    private long maxFileSize;
    
    @Value("${app.upload.allowed-types:pdf,doc,docx,txt,jpg,jpeg,png,gif}")
    private String allowedFileTypes;
    
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar", "sh", "ps1"
    );
    
    @Override
    @Transactional
    public DocumentDto uploadDocument(MultipartFile file, Integer taskId) {
        log.info("Starting document upload for task ID: {}", taskId);
        
        // Validate file
        validateFile(file);
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
        
        return saveDocument(file, task, null);
    }
    
    @Override
    @Transactional
    public DocumentDto uploadDocumentToTodo(MultipartFile file, Integer todoId) {
        log.info("Starting document upload for todo ID: {}", todoId);
        
        // Validate file
        validateFile(file);
        
        Todo todo = todoRepository.findById(todoId)
            .orElseThrow(() -> new RuntimeException("Todo not found with ID: " + todoId));
        
        return saveDocument(file, todo.getTask(), todo);
    }
    
    @Override
    @Transactional
    public DocumentDto uploadDocumentToTemplate(MultipartFile file, Integer templateId) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // Validate file
        validateFile(file);
        
        // For template uploads, associate with the first task of the template
        Task firstTask = taskRepository.findByTemplate_IdOrderByOrderIndexAsc(templateId).stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Template has no tasks to associate document with"));
        
        return uploadDocument(file, firstTask.getId());
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException(String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }
        
        // Check file type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new RuntimeException("Invalid filename");
        }
        
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        
        // Check for dangerous file types
        if (DANGEROUS_EXTENSIONS.contains(fileExtension)) {
            throw new RuntimeException("File type not allowed for security reasons: " + fileExtension);
        }
        
        // Check against allowed file types
        Set<String> allowedTypes = Arrays.stream(allowedFileTypes.split(","))
            .map(String::trim)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
            
        if (!allowedTypes.contains(fileExtension)) {
            throw new RuntimeException("File type not allowed: " + fileExtension + ". Allowed types: " + allowedFileTypes);
        }
        
        // Basic content type validation
        String contentType = file.getContentType();
        if (contentType != null && contentType.contains("script")) {
            throw new RuntimeException("Potentially dangerous content type detected");
        }
        
        log.info("File validation passed for: {} (size: {} bytes, type: {})", 
                originalFilename, file.getSize(), fileExtension);
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }
    
    private DocumentDto saveDocument(MultipartFile file, Task task, Todo todo) {
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
            document.setTodo(todo); // Associate with todo if provided
            document.setRequiresSignature(task.isRequiresSignature());
            document.setFileSize(file.getSize());
            document.setContentType(file.getContentType());
            
            // Save document to database
            Document savedDocument = documentRepository.save(document);
            
            log.info("Document saved successfully with ID: {} for task: {} and todo: {}", 
                    savedDocument.getId(), task.getId(), todo != null ? todo.getId() : "none");
            
            return convertToDto(savedDocument);
            
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public DocumentDto getDocumentById(Integer id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        return convertToDto(document);
    }
    
    @Override
    public List<DocumentDto> getDocumentsByTaskId(Integer taskId) {
        List<Document> documents = documentRepository.findByTaskId(taskId);
        return documents.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DocumentDto> getDocumentsByTodoId(Integer todoId) {
        List<Document> documents = documentRepository.findByTodoId(todoId);
        return documents.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DocumentDto> getDocumentsByTemplateId(Integer templateId) {
        List<Document> documents = documentRepository.findByTask_Template_Id(templateId);
        return documents.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DocumentDto> getDocumentsByHireId(UUID hireId) {
        List<Document> documents = documentRepository.findByTodo_Hire_Id(hireId);
        return documents.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteDocument(Integer id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        
        try {
            // Delete file from disk
            Path filePath = Paths.get(document.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted from disk: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file from disk: {}", e.getMessage(), e);
            // Continue with database deletion even if file deletion fails
        }
        
        documentRepository.deleteById(id);
        log.info("Document deleted from database with ID: {}", id);
    }
    
    @Override
    public byte[] downloadDocument(Integer id, UUID requestingUserId) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        
        // Access control check
        if (!hasAccessToDocument(document, requestingUserId)) {
            throw new RuntimeException("Access denied to document with ID: " + id);
        }
        
        try {
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found on disk: " + document.getFilePath());
            }
            
            byte[] fileData = Files.readAllBytes(filePath);
            log.info("Document downloaded: {} by user: {}", document.getName(), requestingUserId);
            return fileData;
            
        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] downloadDocument(Integer id) {
        // For backward compatibility - no access control
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        
        try {
            Path filePath = Paths.get(document.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }
    
    private boolean hasAccessToDocument(Document document, UUID requestingUserId) {
        // If document is associated with a todo, check if the requesting user is the hire
        if (document.getTodo() != null) {
            return document.getTodo().getHire().getId().equals(requestingUserId);
        }
        
        // If document is associated with a task, check if the requesting user has access
        // This could be expanded based on business rules
        // For now, allow access if the user is associated with any todo for this task
        return todoRepository.findByTask_Id(document.getTask().getId())
            .stream()
            .anyMatch(todo -> todo.getHire().getId().equals(requestingUserId));
    }
    
    @Override
    public String getDocumentDownloadUrl(Integer id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        return "/api/documents/" + id + "/download";
    }
    
    private DocumentDto convertToDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setFilePath(document.getFilePath());
        dto.setDownloadUrl(getDocumentDownloadUrl(document.getId()));
        dto.setRequiresSignature(document.isRequiresSignature());
        dto.setTaskId(document.getTask().getId());
        dto.setTaskTitle(document.getTask().getTitle());
        dto.setTodoId(document.getTodo() != null ? document.getTodo().getId() : null);
        dto.setUploadedAt(document.getUploadedAt());
        dto.setFileSize(document.getFileSize());
        dto.setContentType(document.getContentType());
        return dto;
    }
}