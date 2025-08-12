package com.shaper.server.service;

import com.shaper.server.model.entity.Progress;

import java.util.List;
import java.util.UUID;

public interface ProgressService {
    
    /**
     * Initialize progress tracking for a hire when template is assigned
     */
    Progress initializeProgress(UUID hireId, Integer templateId);
    
    /**
     * Update progress when a todo is completed
     */
    Progress updateProgressOnTodoCompletion(Integer todoId);
    
    /**
     * Recalculate progress for a specific hire and template
     */
    Progress recalculateProgress(UUID hireId, Integer templateId);
    
    /**
     * Get progress for a specific hire and template
     */
    Progress getProgress(UUID hireId, Integer templateId);
    
    /**
     * Get all progress records for a hire
     */
    List<Progress> getProgressByHire(UUID hireId);
    
    /**
     * Get all progress records for hires managed by an HR user
     */
    List<Progress> getProgressByHrUser(UUID hrId);
    
    /**
     * Get progress summary for a company
     */
    List<Progress> getProgressByCompany(Integer companyId);
    
    /**
     * Get progress summary for a department
     */
    List<Progress> getProgressByDepartment(Integer departmentId);
    
    /**
     * Calculate overall completion percentage for a hire across all templates
     */
    Double calculateOverallCompletionPercentage(UUID hireId);
    
    /**
     * Get average completion percentage for hires managed by an HR user
     */
    Double getAverageCompletionByHrUser(UUID hrId);
    
    /**
     * Count completed onboarding processes for an HR user
     */
    long countCompletedOnboardingByHrUser(UUID hrId);
    
    /**
     * Get progress records that are overdue or at risk
     */
    List<Progress> getAtRiskProgress(UUID hrId);
}