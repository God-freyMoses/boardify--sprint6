package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.entity.Hire;
import com.shaper.server.model.entity.Progress;
import com.shaper.server.model.entity.Template;
import com.shaper.server.model.entity.Todo;
import com.shaper.server.model.enums.TodoStatus;
import com.shaper.server.repository.HireRepository;
import com.shaper.server.repository.ProgressRepository;
import com.shaper.server.repository.TemplateRepository;
import com.shaper.server.repository.TodoRepository;
import com.shaper.server.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressServiceImpl implements ProgressService {
    
    private final ProgressRepository progressRepository;
    private final TodoRepository todoRepository;
    private final HireRepository hireRepository;
    private final TemplateRepository templateRepository;
    
    @Override
    @Transactional
    public Progress initializeProgress(UUID hireId, Integer templateId) {
        log.debug("Initializing progress for hire ID: {} and template ID: {}", hireId, templateId);
        
        // Check if progress already exists
        Optional<Progress> existingProgress = progressRepository.findByHireIdAndTemplateId(hireId, templateId);
        if (existingProgress.isPresent()) {
            log.debug("Progress already exists for hire ID: {} and template ID: {}", hireId, templateId);
            return existingProgress.get();
        }
        
        Hire hire = hireRepository.findById(hireId)
            .orElseThrow(() -> new DataNotFoundException("Hire not found with ID: " + hireId));
        
        Template template = templateRepository.findById(templateId)
            .orElseThrow(() -> new DataNotFoundException("Template not found with ID: " + templateId));
        
        // Count total tasks in the template
        long totalTasks = template.getTasks() != null ? template.getTasks().size() : 0;
        
        Progress progress = new Progress();
        progress.setHire(hire);
        progress.setTemplate(template);
        progress.setTotalTasks((int) totalTasks);
        progress.setCompletedTasks(0);
        progress.setCompletionPercentage(0.0);
        progress.setLastUpdated(LocalDateTime.now());
        
        Progress savedProgress = progressRepository.save(progress);
        log.debug("Initialized progress with ID: {} for hire ID: {} and template ID: {}", 
                 savedProgress.getId(), hireId, templateId);
        
        return savedProgress;
    }
    
    @Override
    @Transactional
    public Progress updateProgressOnTodoCompletion(Integer todoId) {
        log.debug("Updating progress for todo ID: {}", todoId);
        
        Todo todo = todoRepository.findById(todoId)
            .orElseThrow(() -> new DataNotFoundException("Todo not found with ID: " + todoId));
        
        return recalculateProgress(todo.getHire().getId(), todo.getTemplate().getId());
    }
    
    @Override
    @Transactional
    public Progress recalculateProgress(UUID hireId, Integer templateId) {
        log.debug("Recalculating progress for hire ID: {} and template ID: {}", hireId, templateId);
        
        Progress progress = progressRepository.findByHireIdAndTemplateId(hireId, templateId)
            .orElseThrow(() -> new DataNotFoundException(
                "Progress not found for hire ID: " + hireId + " and template ID: " + templateId));
        
        // Count completed todos for this hire and template
        long completedTodos = todoRepository.countByHireIdAndStatus(hireId, TodoStatus.COMPLETED);
        List<Todo> allTodos = todoRepository.findByHireIdAndTemplateId(hireId, templateId);
        
        // Filter completed todos for this specific template
        long completedTodosForTemplate = allTodos.stream()
            .mapToLong(todo -> todo.getStatus() == TodoStatus.COMPLETED ? 1 : 0)
            .sum();
        
        int totalTasks = allTodos.size();
        double completionPercentage = totalTasks > 0 ? 
            (completedTodosForTemplate * 100.0) / totalTasks : 0.0;
        
        progress.setTotalTasks(totalTasks);
        progress.setCompletedTasks((int) completedTodosForTemplate);
        progress.setCompletionPercentage(completionPercentage);
        progress.setLastUpdated(LocalDateTime.now());
        
        Progress updatedProgress = progressRepository.save(progress);
        log.debug("Updated progress: {}/{} tasks completed ({}%) for hire ID: {} and template ID: {}", 
                 completedTodosForTemplate, totalTasks, String.format("%.1f", completionPercentage), 
                 hireId, templateId);
        
        return updatedProgress;
    }
    
    @Override
    public Progress getProgress(UUID hireId, Integer templateId) {
        return progressRepository.findByHireIdAndTemplateId(hireId, templateId)
            .orElseThrow(() -> new DataNotFoundException(
                "Progress not found for hire ID: " + hireId + " and template ID: " + templateId));
    }
    
    @Override
    public List<Progress> getProgressByHire(UUID hireId) {
        return progressRepository.findByHire_Id(hireId);
    }
    
    @Override
    public List<Progress> getProgressByHrUser(UUID hrId) {
        return progressRepository.findByHrId(hrId);
    }
    
    @Override
    public List<Progress> getProgressByCompany(Integer companyId) {
        return progressRepository.findByCompanyId(companyId);
    }
    
    @Override
    public List<Progress> getProgressByDepartment(Integer departmentId) {
        return progressRepository.findByDepartmentId(departmentId);
    }
    
    @Override
    public Double calculateOverallCompletionPercentage(UUID hireId) {
        List<Progress> progressList = progressRepository.findByHire_Id(hireId);
        
        if (progressList.isEmpty()) {
            return 0.0;
        }
        
        double totalWeightedCompletion = progressList.stream()
            .mapToDouble(progress -> progress.getCompletionPercentage() * progress.getTotalTasks())
            .sum();
        
        int totalTasks = progressList.stream()
            .mapToInt(Progress::getTotalTasks)
            .sum();
        
        return totalTasks > 0 ? totalWeightedCompletion / totalTasks : 0.0;
    }
    
    @Override
    public Double getAverageCompletionByHrUser(UUID hrId) {
        return progressRepository.getAverageCompletionByHrId(hrId);
    }
    
    @Override
    public long countCompletedOnboardingByHrUser(UUID hrId) {
        return progressRepository.countCompletedByHrId(hrId);
    }
    
    @Override
    public List<Progress> getAtRiskProgress(UUID hrId) {
        // Get all progress records for the HR user
        List<Progress> allProgress = progressRepository.findByHrId(hrId);
        
        // Filter for at-risk progress (less than 50% completion after 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        return allProgress.stream()
            .filter(progress -> progress.getCompletionPercentage() < 50.0 && 
                               progress.getCreatedAt().isBefore(sevenDaysAgo))
            .toList();
    }
}