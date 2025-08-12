package com.shaper.server.repository;

import com.shaper.server.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    
    List<Document> findByTaskId(Integer taskId);
    
    List<Document> findByTodoId(Integer todoId);
    
    List<Document> findByRequiresSignature(boolean requiresSignature);
    
    List<Document> findByTask_Template_Id(Integer templateId);
    
    List<Document> findByTodo_Hire_Id(UUID hireId);
    
    @Query("SELECT d FROM Document d WHERE d.todo.hire.registeredByHr.id = :hrId")
    List<Document> findByHrId(@Param("hrId") UUID hrId);
    
    @Query("SELECT d FROM Document d WHERE d.fileSize > :minSize")
    List<Document> findLargeDocuments(@Param("minSize") Long minSize);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.todo.hire.id = :hireId")
    long countByHireId(@Param("hireId") UUID hireId);
}