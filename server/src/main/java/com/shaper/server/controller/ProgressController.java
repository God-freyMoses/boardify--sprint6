package com.shaper.server.controller;

import com.shaper.server.model.entity.Progress;
import com.shaper.server.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProgressController {
    
    private final ProgressService progressService;
    
    @GetMapping("/hire/{hireId}/template/{templateId}")
    public ResponseEntity<Progress> getProgress(@PathVariable UUID hireId, @PathVariable Integer templateId) {
        try {
            Progress progress = progressService.getProgress(hireId, templateId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/hire/{hireId}")
    public ResponseEntity<List<Progress>> getProgressByHire(@PathVariable UUID hireId) {
        try {
            List<Progress> progressList = progressService.getProgressByHire(hireId);
            return ResponseEntity.ok(progressList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hr/{hrId}")
    public ResponseEntity<List<Progress>> getProgressByHrUser(@PathVariable UUID hrId) {
        try {
            List<Progress> progressList = progressService.getProgressByHrUser(hrId);
            return ResponseEntity.ok(progressList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Progress>> getProgressByCompany(@PathVariable Integer companyId) {
        try {
            List<Progress> progressList = progressService.getProgressByCompany(companyId);
            return ResponseEntity.ok(progressList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Progress>> getProgressByDepartment(@PathVariable Integer departmentId) {
        try {
            List<Progress> progressList = progressService.getProgressByDepartment(departmentId);
            return ResponseEntity.ok(progressList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hire/{hireId}/overall")
    public ResponseEntity<Double> getOverallCompletionPercentage(@PathVariable UUID hireId) {
        try {
            Double percentage = progressService.calculateOverallCompletionPercentage(hireId);
            return ResponseEntity.ok(percentage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hr/{hrId}/average")
    public ResponseEntity<Double> getAverageCompletionByHrUser(@PathVariable UUID hrId) {
        try {
            Double average = progressService.getAverageCompletionByHrUser(hrId);
            return ResponseEntity.ok(average);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hr/{hrId}/completed-count")
    public ResponseEntity<Long> getCompletedOnboardingCount(@PathVariable UUID hrId) {
        try {
            long count = progressService.countCompletedOnboardingByHrUser(hrId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/hr/{hrId}/at-risk")
    public ResponseEntity<List<Progress>> getAtRiskProgress(@PathVariable UUID hrId) {
        try {
            List<Progress> atRiskProgress = progressService.getAtRiskProgress(hrId);
            return ResponseEntity.ok(atRiskProgress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/hire/{hireId}/template/{templateId}/recalculate")
    public ResponseEntity<Progress> recalculateProgress(@PathVariable UUID hireId, @PathVariable Integer templateId) {
        try {
            Progress progress = progressService.recalculateProgress(hireId, templateId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}