package com.shaper.server.repository;

import com.shaper.server.model.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {
    
    List<Todo> findByHire_IdOrderByCreatedAtAsc(UUID hireId);
    
    List<Todo> findByHire_IdAndStatusOrderByDueDateAsc(UUID hireId, com.shaper.server.model.enums.TodoStatus status);
    
    List<Todo> findByTemplate_Id(Integer templateId);
    
    List<Todo> findByTask_Id(Integer taskId);
    
    @Query("SELECT t FROM Todo t WHERE t.hire.id = :hireId AND t.template.id = :templateId ORDER BY t.createdAt ASC")
    List<Todo> findByHireIdAndTemplateId(@Param("hireId") UUID hireId, @Param("templateId") Integer templateId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.hire.id = :hireId AND t.status = :status")
    long countByHireIdAndStatus(@Param("hireId") UUID hireId, @Param("status") com.shaper.server.model.enums.TodoStatus status);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.hire.id = :hireId")
    long countByHireId(@Param("hireId") UUID hireId);
    
    @Query("SELECT t FROM Todo t WHERE t.status = :status AND t.dueDate < :currentDate")
    List<Todo> findOverdueTodos(@Param("status") com.shaper.server.model.enums.TodoStatus status, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Todo t WHERE t.hire.registeredByHr.id = :hrId ORDER BY t.createdAt DESC")
    List<Todo> findByHrId(@Param("hrId") UUID hrId);
}