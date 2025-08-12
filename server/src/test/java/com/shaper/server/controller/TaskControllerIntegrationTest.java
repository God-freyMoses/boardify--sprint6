package com.shaper.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaper.server.model.dto.*;
import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.TaskStatus;
import com.shaper.server.model.enums.TaskType;
import com.shaper.server.model.enums.TemplateStatus;
import com.shaper.server.model.enums.UserRole;
import com.shaper.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// Remove this line: import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private HrUserRepository hrUserRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyDepartmentRepository departmentRepository;

    private Company testCompany;
    private HrUser testHrUser;
    private CompanyDepartment testDepartment;
    private Template testTemplate;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Create test company
        testCompany = new Company();
        testCompany.setName("Test Company " + System.currentTimeMillis());
        testCompany = companyRepository.save(testCompany);

        // Create test HR user
        testHrUser = new HrUser();
        testHrUser.setEmail("hr@test.com");
        testHrUser.setPassword("password");
        testHrUser.setFirstName("HR");
        testHrUser.setLastName("Manager");
        testHrUser.setRole(UserRole.HR_MANAGER); // ✅ This is correct
        testHrUser.setCompany(testCompany);
        testHrUser = hrUserRepository.save(testHrUser);

        // Create test department
        testDepartment = new CompanyDepartment();
        testDepartment.setName("Engineering");
        testDepartment.setCompany(testCompany);
        testDepartment.setCreatedByHr(testHrUser);
        testDepartment = departmentRepository.save(testDepartment);

        // Create test template
        testTemplate = new Template();
        testTemplate.setTitle("Onboarding Template");
        testTemplate.setDescription("Standard onboarding process");
        testTemplate.setStatus(TemplateStatus.PENDING);
        testTemplate.setCreatedByHr(testHrUser);
        testTemplate = templateRepository.save(testTemplate);

        // Create test task
        testTask = new Task();
        testTask.setTitle("Complete Paperwork");
        testTask.setDescription("Fill out all required forms");
        testTask.setTaskType(TaskType.DOCUMENT);
        testTask.setStatus(TaskStatus.PENDING);
        testTask.setRequiresSignature(true);
        testTask.setOrderIndex(1);
        testTask.setTemplate(testTemplate);
        testTask = taskRepository.save(testTask);
    }

    @Test
    @WithMockUser(roles = "HR")
    void createTask_ValidRequest_ShouldReturnCreated() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDescription("New task description");
        request.setTaskType(TaskType.EVENT);
        request.setRequiresSignature(false);
        request.setEventDate(LocalDateTime.now().plusDays(7));

        mockMvc.perform(post("/api/tasks/templates/{templateId}", testTemplate.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task created successfully"))
                .andExpect(jsonPath("$.data.title").value("New Task"))
                .andExpect(jsonPath("$.data.description").value("New task description"))
                .andExpect(jsonPath("$.data.taskType").value("EVENT"))
                .andExpect(jsonPath("$.data.requiresSignature").value(false));
    }

    @Test
    @WithMockUser(roles = "HR")
    void createTask_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        // Missing required fields

        mockMvc.perform(post("/api/tasks/templates/{templateId}", testTemplate.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTaskById_ExistingTask_ShouldReturnTask() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", testTask.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(testTask.getId()))
                .andExpect(jsonPath("$.data.title").value("Complete Paperwork"))
                .andExpect(jsonPath("$.data.taskType").value("DOCUMENT"))
                .andExpect(jsonPath("$.data.requiresSignature").value(true));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTaskById_NonExistentTask_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 99999)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void updateTask_ValidRequest_ShouldReturnUpdated() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated task description");
        request.setTaskType(TaskType.RESOURCE);
        request.setRequiresSignature(false);
        request.setResourceUrl("https://example.com/resource");

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Task"))
                .andExpect(jsonPath("$.data.description").value("Updated task description"))
                .andExpect(jsonPath("$.data.taskType").value("RESOURCE"))
                .andExpect(jsonPath("$.data.requiresSignature").value(false))
                .andExpect(jsonPath("$.data.resourceUrl").value("https://example.com/resource"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTasksByTemplateId_ShouldReturnTasks() throws Exception {
        mockMvc.perform(get("/api/tasks/templates/{templateId}", testTemplate.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tasks retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(roles = "HR")
    void deleteTask_ExistingTask_ShouldReturnSuccess() throws Exception {
        // Create a new task for deletion to avoid affecting other tests
        Task taskToDelete = new Task();
        taskToDelete.setTitle("Task to Delete");
        taskToDelete.setDescription("This task will be deleted");
        taskToDelete.setTaskType(TaskType.DOCUMENT);
        taskToDelete.setStatus(TaskStatus.PENDING);
        taskToDelete.setRequiresSignature(false);
        taskToDelete.setOrderIndex(2);
        taskToDelete.setTemplate(testTemplate);
        taskToDelete = taskRepository.save(taskToDelete);

        mockMvc.perform(delete("/api/tasks/{id}", taskToDelete.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void deleteTask_NonExistentTask_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 99999)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void reorderTasks_ValidRequest_ShouldReturnSuccess() throws Exception {
        // Create a second task for reordering
        Task secondTask = new Task();
        secondTask.setTitle("Second Task");
        secondTask.setDescription("Second task description");
        secondTask.setTaskType(TaskType.EVENT);
        secondTask.setStatus(TaskStatus.PENDING);
        secondTask.setRequiresSignature(false);
        secondTask.setOrderIndex(2);
        secondTask.setTemplate(testTemplate);
        secondTask = taskRepository.save(secondTask);

        ReorderTasksRequest request = new ReorderTasksRequest();
        request.setTemplateId(testTemplate.getId());
        request.setTaskIds(Arrays.asList(secondTask.getId(), testTask.getId())); // Reverse order

        mockMvc.perform(put("/api/tasks/reorder")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tasks reordered successfully"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void moveTaskUp_ValidRequest_ShouldReturnSuccess() throws Exception {
        // Create a second task so we can move the first one up
        Task secondTask = new Task();
        secondTask.setTitle("Second Task");
        secondTask.setDescription("Second task description");
        secondTask.setTaskType(TaskType.EVENT);
        secondTask.setStatus(TaskStatus.PENDING);
        secondTask.setRequiresSignature(false);
        secondTask.setOrderIndex(2);
        secondTask.setTemplate(testTemplate);
        taskRepository.save(secondTask);

        mockMvc.perform(put("/api/tasks/{id}/move-up", secondTask.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task moved up successfully"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void moveTaskDown_ValidRequest_ShouldReturnSuccess() throws Exception {
        // Create a second task so we can move the first one down
        Task secondTask = new Task();
        secondTask.setTitle("Second Task");
        secondTask.setDescription("Second task description");
        secondTask.setTaskType(TaskType.EVENT);
        secondTask.setStatus(TaskStatus.PENDING);
        secondTask.setRequiresSignature(false);
        secondTask.setOrderIndex(2);
        secondTask.setTemplate(testTemplate);
        taskRepository.save(secondTask);

        mockMvc.perform(put("/api/tasks/{id}/move-down", testTask.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task moved down successfully"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTaskCount_ShouldReturnCount() throws Exception {
        mockMvc.perform(get("/api/tasks/templates/{templateId}/count", testTemplate.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task count retrieved successfully"))
                .andExpect(jsonPath("$.data").isNumber())
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @WithMockUser(roles = "HR")
    void createTask_WithEventDate_ShouldReturnCreated() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Team Meeting");
        request.setDescription("Introduction meeting with the team");
        request.setTaskType(TaskType.EVENT);
        request.setRequiresSignature(false);
        request.setEventDate(LocalDateTime.now().plusDays(3));

        mockMvc.perform(post("/api/tasks/templates/{templateId}", testTemplate.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task created successfully"))
                .andExpect(jsonPath("$.data.title").value("Team Meeting"))
                .andExpect(jsonPath("$.data.taskType").value("EVENT"))
                .andExpect(jsonPath("$.data.eventDate").exists());
    }

    @Test
    @WithMockUser(roles = "HR")
    void createTask_WithResourceUrl_ShouldReturnCreated() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Read Company Handbook");
        request.setDescription("Review the company policies and procedures");
        request.setTaskType(TaskType.RESOURCE);
        request.setRequiresSignature(false);
        request.setResourceUrl("https://company.com/handbook.pdf");

        mockMvc.perform(post("/api/tasks/templates/{templateId}", testTemplate.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task created successfully"))
                .andExpect(jsonPath("$.data.title").value("Read Company Handbook"))
                .andExpect(jsonPath("$.data.taskType").value("RESOURCE"))
                .andExpect(jsonPath("$.data.resourceUrl").value("https://company.com/handbook.pdf"));
    }
}