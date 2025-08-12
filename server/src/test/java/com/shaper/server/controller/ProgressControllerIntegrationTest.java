package com.shaper.server.controller;

import com.shaper.server.model.entity.Progress;
import com.shaper.server.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgressController.class)
class ProgressControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProgressService progressService;
    
    @Test
    void getProgress_ShouldReturnProgress() throws Exception {
        // Given
        UUID hireId = UUID.randomUUID();
        Progress progress = new Progress();
        progress.setCompletionPercentage(75.0);
        when(progressService.getProgress(hireId, 1)).thenReturn(progress);
        
        // When & Then
        mockMvc.perform(get("/api/progress/hire/" + hireId + "/template/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionPercentage").value(75.0));
    }
    
    @Test
    void getProgressByHire_ShouldReturnProgressList() throws Exception {
        // Given
        UUID hireId = UUID.randomUUID();
        List<Progress> progressList = Arrays.asList(new Progress(), new Progress());
        when(progressService.getProgressByHire(hireId)).thenReturn(progressList);
        
        // When & Then
        mockMvc.perform(get("/api/progress/hire/" + hireId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void getAtRiskProgress_ShouldReturnAtRiskProgress() throws Exception {
        // Given
        UUID hrId = UUID.randomUUID();
        List<Progress> atRiskProgress = Arrays.asList(new Progress());
        when(progressService.getAtRiskProgress(hrId)).thenReturn(atRiskProgress);
        
        // When & Then
        mockMvc.perform(get("/api/progress/hr/" + hrId + "/at-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
    
    @Test
    void recalculateProgress_ShouldReturnUpdatedProgress() throws Exception {
        // Given
        UUID hireId = UUID.randomUUID();
        Progress progress = new Progress();
        progress.setCompletionPercentage(80.0);
        when(progressService.recalculateProgress(hireId, 1)).thenReturn(progress);
        
        // When & Then
        mockMvc.perform(post("/api/progress/hire/" + hireId + "/template/1/recalculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionPercentage").value(80.0));
    }
}