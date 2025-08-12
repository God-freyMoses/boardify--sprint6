package com.shaper.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaper.server.model.dto.*;
import com.shaper.server.model.entity.*;
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

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// Remove this line: import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class TemplateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private HrUserRepository hrUserRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyDepartmentRepository departmentRepository;

    @Autowired
    private HireRepository hireRepository;

    private Company testCompany;
    private HrUser testHrUser;
    private CompanyDepartment testDepartment;
    private Template testTemplate;
    private Hire testHire;

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
        testHrUser.setRole(UserRole.HR_MANAGER);
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

        // Create test hire
        testHire = new Hire();
        testHire.setEmail("hire@test.com");
        testHire.setPassword("password");
        testHire.setFirstName("New");
        testHire.setLastName("Hire");
        testHire.setRole(UserRole.NEW_HIRE);
        testHire.setTitle("Software Engineer");
        testHire.setRegisteredByHr(testHrUser);
        testHire.setDepartment(testDepartment);
        testHire = hireRepository.save(testHire);
    }

    @Test
    @WithMockUser(roles = "HR")
    void createTemplate_ValidRequest_ShouldReturnCreated() throws Exception {
        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setTitle("New Template");
        request.setDescription("New template description");
        request.setHrId(testHrUser.getId());
        request.setDepartmentIds(Arrays.asList(testDepartment.getId()));

        mockMvc.perform(post("/api/templates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template created successfully"))
                .andExpect(jsonPath("$.data.title").value("New Template"))
                .andExpect(jsonPath("$.data.description").value("New template description"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void createTemplate_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        CreateTemplateRequest request = new CreateTemplateRequest();
        // Missing required fields

        mockMvc.perform(post("/api/templates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTemplateById_ExistingTemplate_ShouldReturnTemplate() throws Exception {
        mockMvc.perform(get("/api/templates/{id}", testTemplate.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(testTemplate.getId()))
                .andExpect(jsonPath("$.data.title").value("Onboarding Template"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTemplateById_NonExistentTemplate_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/templates/{id}", 99999)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Template not found"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void updateTemplate_ValidRequest_ShouldReturnUpdated() throws Exception {
        UpdateTemplateRequest request = new UpdateTemplateRequest();
        request.setTitle("Updated Template");
        request.setDescription("Updated description");
        request.setStatus("IN_PROGRESS");
        request.setDepartmentIds(Arrays.asList(testDepartment.getId()));

        mockMvc.perform(put("/api/templates/{id}", testTemplate.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Template"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getAllTemplates_ShouldReturnTemplates() throws Exception {
        mockMvc.perform(get("/api/templates")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Templates retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTemplatesByHrId_ShouldReturnHrTemplates() throws Exception {
        mockMvc.perform(get("/api/templates/hr/{hrId}", testHrUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Templates retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTemplatesByCompanyId_ShouldReturnCompanyTemplates() throws Exception {
        mockMvc.perform(get("/api/templates/company/{companyId}", testCompany.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Templates retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "HR")
    void getTemplatesByDepartmentId_ShouldReturnDepartmentTemplates() throws Exception {
        mockMvc.perform(get("/api/templates/department/{departmentId}", testDepartment.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Templates retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "HR")
    void assignTemplateToHire_ValidRequest_ShouldReturnSuccess() throws Exception {
        // First add a task to the template to make it assignable
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test task description");
        task.setTaskType(com.shaper.server.model.enums.TaskType.DOCUMENT);
        task.setTemplate(testTemplate);
        task.setOrderIndex(1);
        testTemplate.getTasks().add(task);
        templateRepository.save(testTemplate);

        mockMvc.perform(post("/api/templates/{templateId}/assign/{hireId}", 
                testTemplate.getId(), testHire.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template assigned successfully"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void assignTemplateToMultipleHires_ValidRequest_ShouldReturnSuccess() throws Exception {
        // First add a task to the template to make it assignable
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test task description");
        task.setTaskType(com.shaper.server.model.enums.TaskType.DOCUMENT);
        task.setTemplate(testTemplate);
        task.setOrderIndex(1);
        testTemplate.getTasks().add(task);
        templateRepository.save(testTemplate);

        AssignTemplateRequest request = new AssignTemplateRequest();
        request.setTemplateId(testTemplate.getId());
        request.setHireIds(Arrays.asList(testHire.getId()));

        mockMvc.perform(post("/api/templates/assign")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template assigned to 1 hires successfully"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void deleteTemplate_ExistingTemplate_ShouldReturnSuccess() throws Exception {
        // Create a new template for deletion to avoid affecting other tests
        Template templateToDelete = new Template();
        templateToDelete.setTitle("Template to Delete");
        templateToDelete.setDescription("This template will be deleted");
        templateToDelete.setStatus(TemplateStatus.PENDING);
        templateToDelete.setCreatedByHr(testHrUser);
        templateToDelete = templateRepository.save(templateToDelete);

        mockMvc.perform(delete("/api/templates/{id}", templateToDelete.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "HR")
    void deleteTemplate_NonExistentTemplate_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/templates/{id}", 99999)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Template not found"));
    }
}