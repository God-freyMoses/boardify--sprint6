package com.shaper.server.service;

import com.shaper.server.model.dto.DocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    
    DocumentDto uploadDocument(MultipartFile file, Integer taskId);
    
    DocumentDto uploadDocumentToTemplate(MultipartFile file, Integer templateId);
    
    DocumentDto getDocumentById(Integer id);
    
    List<DocumentDto> getDocumentsByTaskId(Integer taskId);
    
    List<DocumentDto> getDocumentsByTemplateId(Integer templateId);
    
    void deleteDocument(Integer id);
    
    byte[] downloadDocument(Integer id);
    
    String getDocumentDownloadUrl(Integer id);
}