package com.shaper.server.service.impl;

import com.shaper.server.model.dto.DocumentDto;
import com.shaper.server.model.entity.Document;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.entity.Todo;
import com.shaper.server.model.entity.Hire;
import com.shaper.server.model.enums.TaskType;
import com.shaper.server.repository.DocumentRepository;
import com.shaper.server.repository.TaskRepository;
import com.shaper.server.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private TodoRepository todoRepository;
    
    @InjectMocks
    private DocumentServiceImpl documentService;
    
    @TempDir
    Path tempDir;
    
    private Task testTask;
    private Todo testTodo;
    private Hire testHire;
    private Document testDocument;
    private MultipartFile validFile;
    private MultipartFile invalidFile;
    private MultipartFile oversizedFile;
    private MultipartFile dangerousFile;
    
    @BeforeEach
    void setUp() {
        // Set up test upload directory
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(documentService, "maxFileSize", 1048576L); // 1MB
        ReflectionTestUtils.setField(documentService, "allowedFileTypes", "pdf,doc,docx,txt,jpg,jpeg,png");
        
        // Create test entities
        testTask = new Task();
        testTask.setId(1);
        testTask.setTitle("Test Task");
        testTask.setTaskType(TaskType.DOCUMENT);
        testTask.setRequiresSignature(true);
        
        testHire = new Hire();
        testHire.setId(UUID.randomUUID());
        testHire.setFirstName("John");
        testHire.setLastName("Doe");
        
        testTodo = new Todo();
        testTodo.setId(1);
        testTodo.setTask(testTask);
        testTodo.setHire(testHire);
        
        testDocument = new Document();
        testDocument.setId(1);
        testDocument.setName("test-document.pdf");
        testDocument.setFilePath(tempDir.resolve("test-file.pdf").toString());
        testDocument.setTask(testTask);
        testDocument.setTodo(testTodo);
        testDocument.setFileSize(1024L);
        testDocument.setContentType("application/pdf");
        testDocument.setUploadedAt(LocalDateTime.now());
        
        // Create test files
        validFile = new MockMultipartFile(
            "file", 
            "test-document.pdf", 
            "application/pdf", 
            "Test content".getBytes()
        );
        
        invalidFile = new MockMultipartFile(
            "file", 
            "test-document.xyz", 
            "application/octet-stream", 
            "Test content".getBytes()
        );
        
        // Create oversized file (2MB when limit is 1MB)
        byte[] largeContent = new byte[2 * 1024 * 1024];
        oversizedFile = new MockMultipartFile(
            "file", 
            "large-document.pdf", 
            "application/pdf", 
            largeContent
        );
        
        dangerousFile = new MockMultipartFile(
            "file", 
            "malicious.exe", 
            "application/octet-stream", 
            "Malicious content".getBytes()
        );
    }
    
    @Test
    void uploadDocument_ShouldUploadSuccessfully() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // When
        DocumentDto result = documentService.uploadDocument(validFile, 1);
        
        // Then
        assertNotNull(result);
        assertEquals("test-document.pdf", result.getName());
        assertEquals(1, result.getTaskId());
        assertTrue(result.isRequiresSignature());
        
        verify(taskRepository).findById(1);
        verify(documentRepository).save(any(Document.class));
    }
    
    @Test
    void uploadDocument_ShouldThrowException_WhenTaskNotFound() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.uploadDocument(validFile, 1));
        
        assertEquals("Task not found with ID: 1", exception.getMessage());
        verify(taskRepository).findById(1);
        verify(documentRepository, never()).save(any(Document.class));
    }
    
    @Test
    void uploadDocument_ShouldThrowException_WhenFileIsEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.uploadDocument(emptyFile, 1));
        
        assertEquals("File is empty", exception.getMessage());
        verify(taskRepository, never()).findById(anyInt());
    }
    
    @Test
    void uploadDocument_ShouldThrowException_WhenFileTypeNotAllowed() {
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.uploadDocument(invalidFile, 1));
        
        assertTrue(exception.getMessage().contains("File type not allowed: xyz"));
    }
    
    @Test
    void uploadDocument_ShouldThrowException_WhenFileSizeExceedsLimit() {
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.uploadDocument(oversizedFile, 1));
        
        assertTrue(exception.getMessage().contains("File size exceeds maximum allowed size"));
    }
    
    @Test
    void uploadDocument_ShouldThrowException_WhenFileTypeIsDangerous() {
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.uploadDocument(dangerousFile, 1));
        
        assertEquals("File type not allowed for security reasons: exe", exception.getMessage());
    }
    
    @Test
    void uploadDocumentToTodo_ShouldUploadSuccessfully() {
        // Given
        when(todoRepository.findById(1)).thenReturn(Optional.of(testTodo));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // When
        DocumentDto result = documentService.uploadDocumentToTodo(validFile, 1);
        
        // Then
        assertNotNull(result);
        assertEquals("test-document.pdf", result.getName());
        assertEquals(1, result.getTodoId());
        
        verify(todoRepository).findById(1);
        verify(documentRepository).save(any(Document.class));
    }
    
    @Test
    void uploadDocumentToTodo_ShouldThrowException_WhenTodoNotFound() {
        // Given
        when(todoRepository.findById(1)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.uploadDocumentToTodo(validFile, 1));
        
        assertEquals("Todo not found with ID: 1", exception.getMessage());
    }
    
    @Test
    void getDocumentById_ShouldReturnDocument() {
        // Given
        when(documentRepository.findById(1)).thenReturn(Optional.of(testDocument));
        
        // When
        DocumentDto result = documentService.getDocumentById(1);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test-document.pdf", result.getName());
        verify(documentRepository).findById(1);
    }
    
    @Test
    void getDocumentById_ShouldThrowException_WhenDocumentNotFound() {
        // Given
        when(documentRepository.findById(1)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.getDocumentById(1));
        
        assertEquals("Document not found with ID: 1", exception.getMessage());
    }
    
    @Test
    void getDocumentsByTaskId_ShouldReturnDocuments() {
        // Given
        List<Document> documents = Arrays.asList(testDocument);
        when(documentRepository.findByTaskId(1)).thenReturn(documents);
        
        // When
        List<DocumentDto> result = documentService.getDocumentsByTaskId(1);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-document.pdf", result.get(0).getName());
        verify(documentRepository).findByTaskId(1);
    }
    
    @Test
    void getDocumentsByTodoId_ShouldReturnDocuments() {
        // Given
        List<Document> documents = Arrays.asList(testDocument);
        when(documentRepository.findByTodoId(1)).thenReturn(documents);
        
        // When
        List<DocumentDto> result = documentService.getDocumentsByTodoId(1);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-document.pdf", result.get(0).getName());
        verify(documentRepository).findByTodoId(1);
    }
    
    @Test
    void getDocumentsByHireId_ShouldReturnDocuments() {
        // Given
        UUID hireId = testHire.getId();
        List<Document> documents = Arrays.asList(testDocument);
        when(documentRepository.findByTodo_Hire_Id(hireId)).thenReturn(documents);
        
        // When
        List<DocumentDto> result = documentService.getDocumentsByHireId(hireId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-document.pdf", result.get(0).getName());
        verify(documentRepository).findByTodo_Hire_Id(hireId);
    }
    
    @Test
    void deleteDocument_ShouldDeleteSuccessfully() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test-file.pdf");
        Files.createFile(testFile);
        testDocument.setFilePath(testFile.toString());
        
        when(documentRepository.findById(1)).thenReturn(Optional.of(testDocument));
        
        // When
        documentService.deleteDocument(1);
        
        // Then
        assertFalse(Files.exists(testFile));
        verify(documentRepository).findById(1);
        verify(documentRepository).deleteById(1);
    }
    
    @Test
    void deleteDocument_ShouldThrowException_WhenDocumentNotFound() {
        // Given
        when(documentRepository.findById(1)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.deleteDocument(1));
        
        assertEquals("Document not found with ID: 1", exception.getMessage());
        verify(documentRepository, never()).deleteById(anyInt());
    }
    
    @Test
    void downloadDocument_ShouldReturnFileData() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test-file.pdf");
        String testContent = "Test file content";
        Files.write(testFile, testContent.getBytes());
        testDocument.setFilePath(testFile.toString());
        
        when(documentRepository.findById(1)).thenReturn(Optional.of(testDocument));
        
        // When
        byte[] result = documentService.downloadDocument(1);
        
        // Then
        assertNotNull(result);
        assertEquals(testContent, new String(result));
        verify(documentRepository).findById(1);
    }
    
    @Test
    void downloadDocument_WithAccessControl_ShouldReturnFileData_WhenUserHasAccess() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test-file.pdf");
        String testContent = "Test file content";
        Files.write(testFile, testContent.getBytes());
        testDocument.setFilePath(testFile.toString());
        
        when(documentRepository.findById(1)).thenReturn(Optional.of(testDocument));
        
        // When
        byte[] result = documentService.downloadDocument(1, testHire.getId());
        
        // Then
        assertNotNull(result);
        assertEquals(testContent, new String(result));
        verify(documentRepository).findById(1);
    }
    
    @Test
    void downloadDocument_WithAccessControl_ShouldThrowException_WhenUserHasNoAccess() {
        // Given
        UUID unauthorizedUserId = UUID.randomUUID();
        when(documentRepository.findById(1)).thenReturn(Optional.of(testDocument));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.downloadDocument(1, unauthorizedUserId));
        
        assertEquals("Access denied to document with ID: 1", exception.getMessage());
    }
    
    @Test
    void downloadDocument_ShouldThrowException_WhenFileNotFoundOnDisk() {
        // Given
        testDocument.setFilePath("/non/existent/path/file.pdf");
        when(documentRepository.findById(1)).thenReturn(Optional.of(testDocument));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> documentService.downloadDocument(1));
        
        assertTrue(exception.getMessage().contains("Failed to read file"));
    }
    
    @Test
    void getDocumentDownloadUrl_ShouldReturnCorrectUrl() {
        // Given
        when(documentRepository.findById(1)).thenReturn(Optional.of(testDocument));
        
        // When
        String result = documentService.getDocumentDownloadUrl(1);
        
        // Then
        assertEquals("/api/documents/1/download", result);
        verify(documentRepository).findById(1);
    }
}