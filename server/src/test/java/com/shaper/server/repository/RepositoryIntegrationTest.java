package com.shaper.server.repository;

import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.TaskType;
import com.shaper.server.model.enums.TodoStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private HrUserRepository hrUserRepository;
    
    @Autowired
    private HireRepository hireRepository;
    
    @Autowired
    private CompanyDepartmentRepository departmentRepository;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TodoRepository todoRepository;
    
    @Autowired
    private ProgressRepository progressRepository;

    private Company company;
    private HrUser hrUser;
    private CompanyDepartment department;
    private Hire hire;
    private Template template;
    private Task task;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setName("Test Company");
        company = companyRepository.save(company);

        hrUser = new HrUser();
        hrUser.setEmail("hr@test.com");
        hrUser.setPassword("password123");
        hrUser.setFirstName("HR");
        hrUser.setLastName("Manager");
        hrUser.setCompany(company);
        hrUser = hrUserRepository.save(hrUser);

        department = new CompanyDepartment();
        department.setName("Engineering");
        department.setCompany(company);
        department.setCreatedByHr(hrUser);
        department = departmentRepository.save(department);

        hire = new Hire();
        hire.setEmail("hire@test.com");
        hire.setPassword("password123");
        hire.setFirstName("John");
        hire.setLastName("Doe");
        hire.setTitle("Software Engineer");
        hire.setRegisteredByHr(hrUser);
        hire.setDepartment(department);
        hire = hireRepository.save(hire);

        template = new Template();
        template.setTitle("Test Template");
        template.setDescription("Test Description");
        template.setCreatedByHr(hrUser);
        template = templateRepository.save(template);

        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test task description");
        task.setTaskType(TaskType.DOCUMENT);
        task.setRequiresSignature(false);
        task.setOrderIndex(1);
        task.setTemplate(template);
        task = taskRepository.save(task);
    }

    @Test
    void shouldFindHrUserByEmail() {
        // When
        Optional<HrUser> found = hrUserRepository.findByEmail("hr@test.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("HR");
        assertThat(found.get().getCompany()).isEqualTo(company);
    }

    @Test
    void shouldFindHiresByDepartment() {
        // When
        List<Hire> hires = hireRepository.findByDepartment_Id(department.getId());

        // Then
        assertThat(hires).hasSize(1);
        assertThat(hires.get(0)).isEqualTo(hire);
    }

    @Test
    void shouldFindTemplatesByHrUser() {
        // When
        List<Template> templates = templateRepository.findByCreatedByHr_Id(hrUser.getId());

        // Then
        assertThat(templates).hasSize(1);
        assertThat(templates.get(0)).isEqualTo(template);
    }

    @Test
    void shouldFindTasksByTemplateOrderedByIndex() {
        // Given
        Task task2 = new Task();
        task2.setTitle("Second Task");
        task2.setDescription("Second task description");
        task2.setTaskType(TaskType.EVENT);
        task2.setRequiresSignature(false);
        task2.setOrderIndex(2);
        task2.setTemplate(template);
        taskRepository.save(task2);

        // When
        List<Task> tasks = taskRepository.findByTemplate_IdOrderByOrderIndexAsc(template.getId());

        // Then
        assertThat(tasks).hasSize(2);
        assertThat(tasks.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(tasks.get(1).getOrderIndex()).isEqualTo(2);
    }

    @Test
    void shouldFindTodosByHireAndStatus() {
        // Given
        Todo todo1 = createTodo(TodoStatus.PENDING);
        Todo todo2 = createTodo(TodoStatus.COMPLETED);
        todoRepository.save(todo1);
        todoRepository.save(todo2);

        // When
        List<Todo> pendingTodos = todoRepository.findByHire_IdAndStatusOrderByDueDateAsc(
                hire.getId(), TodoStatus.PENDING);

        // Then
        assertThat(pendingTodos).hasSize(1);
        assertThat(pendingTodos.get(0).getStatus()).isEqualTo(TodoStatus.PENDING);
    }

    @Test
    void shouldCountTodosByHireAndStatus() {
        // Given
        Todo todo1 = createTodo(TodoStatus.PENDING);
        Todo todo2 = createTodo(TodoStatus.COMPLETED);
        Todo todo3 = createTodo(TodoStatus.PENDING);
        todoRepository.save(todo1);
        todoRepository.save(todo2);
        todoRepository.save(todo3);

        // When
        long pendingCount = todoRepository.countByHireIdAndStatus(hire.getId(), TodoStatus.PENDING);
        long completedCount = todoRepository.countByHireIdAndStatus(hire.getId(), TodoStatus.COMPLETED);

        // Then
        assertThat(pendingCount).isEqualTo(2);
        assertThat(completedCount).isEqualTo(1);
    }

    @Test
    void shouldFindProgressByHireAndTemplate() {
        // Given
        Progress progress = new Progress();
        progress.setHire(hire);
        progress.setTemplate(template);
        progress.setTotalTasks(5);
        progress.setCompletedTasks(2);
        progressRepository.save(progress);

        // When
        Optional<Progress> found = progressRepository.findByHireIdAndTemplateId(
                hire.getId(), template.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTotalTasks()).isEqualTo(5);
        assertThat(found.get().getCompletedTasks()).isEqualTo(2);
    }

    @Test
    void shouldFindDepartmentsByCompany() {
        // Given
        CompanyDepartment department2 = new CompanyDepartment();
        department2.setName("Marketing");
        department2.setCompany(company);
        department2.setCreatedByHr(hrUser);
        departmentRepository.save(department2);

        // When
        List<CompanyDepartment> departments = departmentRepository.findByCompany_Id(company.getId());

        // Then
        assertThat(departments).hasSize(2);
        assertThat(departments).extracting(CompanyDepartment::getName)
                .containsExactlyInAnyOrder("Engineering", "Marketing");
    }

    @Test
    void shouldCheckDepartmentNameUniquenessWithinCompany() {
        // When
        boolean exists = departmentRepository.existsByNameAndCompany_Id("Engineering", company.getId());
        boolean notExists = departmentRepository.existsByNameAndCompany_Id("NonExistent", company.getId());

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    private Todo createTodo(TodoStatus status) {
        Todo todo = new Todo();
        todo.setHire(hire);
        todo.setTask(task);
        todo.setTemplate(template);
        todo.setStatus(status);
        todo.setDueDate(LocalDateTime.now().plusDays(7));
        return todo;
    }
}