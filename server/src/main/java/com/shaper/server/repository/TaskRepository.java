package com.shaper.server.repository;

import com.shaper.server.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    
    List<Task> findByStatus(com.shaper.server.model.enums.TaskStatus status);
    
    List<Task> findByTaskType(com.shaper.server.model.enums.TaskType taskType);
    
    List<Task> findByRequiresSignature(boolean requiresSignature);
    
    List<Task> findByTemplate_IdOrderByOrderIndexAsc(Integer templateId);
    
    @Query("SELECT t FROM Task t WHERE t.template.id = :templateId ORDER BY t.orderIndex ASC")
    List<Task> findByTemplateIdOrderedByIndex(@Param("templateId") Integer templateId);
    
    @Query("SELECT t FROM Task t WHERE t.template.createdByHr.id = :hrId")
    List<Task> findByHrId(@Param("hrId") java.util.UUID hrId);
    
    @Query("SELECT MAX(t.orderIndex) FROM Task t WHERE t.template.id = :templateId")
    Integer findMaxOrderIndexByTemplateId(@Param("templateId") Integer templateId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.template.id = :templateId")
    long countByTemplateId(@Param("templateId") Integer templateId);
}