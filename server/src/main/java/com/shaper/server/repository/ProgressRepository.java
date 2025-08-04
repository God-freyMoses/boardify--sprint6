package com.shaper.server.repository;

import com.shaper.server.model.entity.Progress;
import com.shaper.server.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Integer> {
    
    List<Progress> findByHire_Id(UUID hireId);
    
    List<Progress> findByTask_Id(Integer taskId);
    
    List<Progress> findByHire_IdAndStatus(UUID hireId, Task.TaskStatus status);
    
    @Query("SELECT p FROM Progress p WHERE p.hire.id = :hireId AND p.task.id = :taskId")
    Progress findByHireIdAndTaskId(@Param("hireId") UUID hireId, @Param("taskId") Integer taskId);
    
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.hire.id = :hireId AND p.status = :status")
    long countByHireIdAndStatus(@Param("hireId") UUID hireId, @Param("status") Task.TaskStatus status);
    
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.hire.id = :hireId")
    long countByHireId(@Param("hireId") UUID hireId);
}