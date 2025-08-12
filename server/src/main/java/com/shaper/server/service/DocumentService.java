package com.shaper.server.service;

import com.shaper.server.model.dto.DocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    
    DocumentDto uploadDocument(MultipartFile file, Integer taskId);
    
    DocumentDto uploadDocumentToTodo(MultipartFile file, Integer todoId);
    
    DocumentDto uploadDocumentToTemplate(MultipartFile file, Integer templateId);
    
    DocumentDto getDocumentById(Integer id);
    
    List<DocumentDto> getDocumentsByTaskId(Integer taskId);
    
    List<DocumentDto> getDocumentsByTodoId(Integer todoId);
    
    List<DocumentDto> getDocumentsByTemplateId(Integer templateId);
    
    List<DocumentDto> getDocumentsByHireId(UUID hireId);
    
    void deleteDocument(Integer id);
    
    byte[] downloadDocument(Integer id);
    
    byte[] downloadDocument(Integer id, UUID requestingUserId);
    
    String getDocumentDownloadUrl(Integer id);
}