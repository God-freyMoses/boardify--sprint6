package com.shaper.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaper.server.model.dto.NotificationDto;
import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.NotificationType;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HrUserRepository hrUserRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyDepartmentRepository departmentRepository;

    private User testUser;
    private HrUser testHrUser;
    private Task testTask;
    private Notification testNotification;
    private Company testCompany;
    private CompanyDepartment testDepartment;
    private Template testTemplate;

    @BeforeEach
    void setUp() {
        // Create test company
        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setAddress("123 Test St");
        testCompany.setContactEmail("test@company.com");
        testCompany.setContactPhone("+1234567890");
        testCompany = companyRepository.save(testCompany);

        // Create test department
        testDepartment = new CompanyDepartment();
        testDepartment.setName("Test Department");
        testDepartment.setCompany(testCompany);
        testDepartment = departmentRepository.save(testDepartment);

        // Create test HR user
        testHrUser = new HrUser();
        testHrUser.setId(UUID.randomUUID());
        testHrUser.setEmail("hr@test.com");
        testHrUser.setPassword("password");
        testHrUser.setFirstName("HR");
        testHrUser.setLastName("Manager");
        testHrUser.setRole(UserRole.HR_MANAGER);
        testHrUser.setCompany(testCompany);
        testHrUser = hrUserRepository.save(testHrUser);

        // Create test user
        testUser = new User() {};
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@test.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.NEW_HIRE);
        testUser = userRepository.save(testUser);

        // Create test template
        testTemplate = new Template();
        testTemplate.setTitle("Test Template");
        testTemplate.setDescription("Test template description");
        testTemplate.setHrUser(testHrUser);
        testTemplate = templateRepository.save(testTemplate);

        // Create test task
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test task description");
        testTask.setTemplate(testTemplate);
        testTask.setRequiresSignature(true);
        testTask = taskRepository.save(testTask);

        // Create test notification
        testNotification = new Notification();
        testNotification.setUser(testUser);
        testNotification.setMessage("Test notification");
        testNotification.setRead(false);
        testNotification.setRelatedTask(testTask);
        testNotification.setNotificationType(NotificationType.REMINDER);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification = notificationRepository.save(testNotification);
    }

    @Test
    @WithMockUser(roles = "HR")
    void createNotification_ValidRequest_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/notifications")
                .with(csrf())
                .param("userId", testUser.getId().toString())
                .param("message", "New test notification")
                .param("relatedTaskId", testTask.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.message").value("New test notification"))
                .andExpect(jsonPath("$.relatedTaskId").value(testTask.getId()))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getNotificationsByUserId_ShouldReturnNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/user/{userId}", testUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].message").value("Test notification"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getUnreadNotificationsByUserId_ShouldReturnUnreadNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/user/{userId}/unread", testUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @WithMockUser(roles = "HR")
    void markAsRead_ValidId_ShouldReturnUpdatedNotification() throws Exception {
        mockMvc.perform(put("/api/notifications/{id}/read", testNotification.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @WithMockUser(roles = "HR")
    void markAllAsRead_ValidUserId_ShouldReturnOk() throws Exception {
        mockMvc.perform(put("/api/notifications/user/{userId}/read-all", testUser.getId())
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "HR")
    void getUnreadCount_ShouldReturnCount() throws Exception {
        mockMvc.perform(get("/api/notifications/user/{userId}/unread-count", testUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber())
                .andExpect(jsonPath("$").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @WithMockUser(roles = "HR")
    void sendManualReminder_ValidRequest_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/notifications/reminders/manual")
                .with(csrf())
                .param("userId", testUser.getId().toString())
                .param("taskId", testTask.getId().toString())
                .param("customMessage", "Please complete this task soon"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Please complete this task soon"))
                .andExpect(jsonPath("$.notificationType").value("REMINDER"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void sendSignatureRequest_ValidRequest_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/notifications/signature-requests")
                .with(csrf())
                .param("userId", testUser.getId().toString())
                .param("taskId", testTask.getId().toString()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "HR")
    void sendOverdueTaskNotification_ValidRequest_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/notifications/overdue-tasks")
                .with(csrf())
                .param("userId", testUser.getId().toString())
                .param("taskId", testTask.getId().toString()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "HR")
    void sendBulkReminders_ValidRequest_ShouldReturnCreated() throws Exception {
        // Create another test user
        User testUser2 = new User() {};
        testUser2.setId(UUID.randomUUID());
        testUser2.setEmail("user2@test.com");
        testUser2.setPassword("password");
        testUser2.setFirstName("Test2");
        testUser2.setLastName("User2");
        testUser2.setRole(UserRole.NEW_HIRE);
        testUser2 = userRepository.save(testUser2);

        mockMvc.perform(post("/api/notifications/reminders/bulk")
                .with(csrf())
                .param("userIds", testUser.getId().toString(), testUser2.getId().toString())
                .param("taskId", testTask.getId().toString())
                .param("customMessage", "Bulk reminder message"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "HR")
    void getNotificationBadgeCount_ShouldReturnBadgeInfo() throws Exception {
        mockMvc.perform(get("/api/notifications/user/{userId}/badge-count", testUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").isNumber())
                .andExpect(jsonPath("$.recentNotifications").isArray())
                .andExpect(jsonPath("$.hasUrgent").isBoolean());
    }

    @Test
    @WithMockUser(roles = "HR")
    void getHRDashboardNotifications_ShouldReturnNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/hr/dashboard")
                .with(csrf())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createNotification_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/notifications")
                .param("userId", testUser.getId().toString())
                .param("message", "Unauthorized notification"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void sendManualReminder_WithoutHRRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/notifications/reminders/manual")
                .with(csrf())
                .param("userId", testUser.getId().toString())
                .param("taskId", testTask.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "HR")
    void markAsRead_NonExistentId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/notifications/{id}/read", 99999)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    void createNotification_InvalidUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/notifications")
                .with(csrf())
                .param("userId", UUID.randomUUID().toString())
                .param("message", "Invalid user notification"))
                .andExpect(status().isBadRequest());
    }
}