package com.shaper.server.repository;

import com.shaper.server.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    
    List<Document> findByTaskId(Integer taskId);
    
    List<Document> findByRequiresSignature(boolean requiresSignature);
    
    List<Document> findByTask_Template_Id(Integer templateId);
}