package com.shaper.server.repository;

import com.shaper.server.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    
    List<Task> findByStatus(Task.TaskStatus status);
    
    List<Task> findByRequiresSignature(boolean requiresSignature);
    
    @Query("SELECT t FROM Task t JOIN t.templates temp WHERE temp.id = :templateId ORDER BY t.createdAt ASC")
    List<Task> findByTemplateId(@Param("templateId") Integer templateId);
    
    @Query("SELECT t FROM Task t JOIN t.templates temp JOIN temp.createdByHr hr WHERE hr.id = :hrId")
    List<Task> findByHrId(@Param("hrId") java.util.UUID hrId);
}