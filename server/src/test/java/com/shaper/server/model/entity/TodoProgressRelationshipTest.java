package com.shaper.server.model.entity;

import com.shaper.server.model.enums.TaskType;
import com.shaper.server.model.enums.TodoStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TodoProgressRelationshipTest {

    @Autowired
    private TestEntityManager entityManager;

    private Hire hire;
    private Template template;
    private Task task;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setName("Test Company");
        entityManager.persistAndFlush(company);

        HrUser hrUser = new HrUser();
        hrUser.setEmail("hr@test.com");
        hrUser.setPassword("password123");
        hrUser.setFirstName("HR");
        hrUser.setLastName("Manager");
        hrUser.setCompany(company);
        entityManager.persistAndFlush(hrUser);

        CompanyDepartment department = new CompanyDepartment();
        department.setName("Engineering");
        department.setCompany(company);
        department.setCreatedByHr(hrUser);
        entityManager.persistAndFlush(department);

        hire = new Hire();
        hire.setEmail("hire@test.com");
        hire.setPassword("password123");
        hire.setFirstName("John");
        hire.setLastName("Doe");
        hire.setTitle("Software Engineer");
        hire.setRegisteredByHr(hrUser);
        hire.setDepartment(department);
        entityManager.persistAndFlush(hire);

        template = new Template();
        template.setTitle("Test Template");
        template.setDescription("Test Description");
        template.setCreatedByHr(hrUser);
        entityManager.persistAndFlush(template);

        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test task description");
        task.setTaskType(TaskType.DOCUMENT);
        task.setRequiresSignature(false);
        task.setOrderIndex(1);
        task.setTemplate(template);
        entityManager.persistAndFlush(task);
    }

    @Test
    void shouldCreateTodoWithValidRelationships() {
        // Given
        Todo todo = new Todo();
        todo.setHire(hire);
        todo.setTask(task);
        todo.setTemplate(template);
        todo.setDueDate(LocalDateTime.now().plusDays(7));

        // When
        Todo savedTodo = entityManager.persistAndFlush(todo);

        // Then
        assertThat(savedTodo.getId()).isNotNull();
        assertThat(savedTodo.getHire()).isEqualTo(hire);
        assertThat(savedTodo.getTask()).isEqualTo(task);
        assertThat(savedTodo.getTemplate()).isEqualTo(template);
        assertThat(savedTodo.getStatus()).isEqualTo(TodoStatus.PENDING);
        assertThat(savedTodo.getCreatedAt()).isNotNull();
        assertThat(savedTodo.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldSetCompletedAtWhenStatusChangesToCompleted() {
        // Given
        Todo todo = new Todo();
        todo.setHire(hire);
        todo.setTask(task);
        todo.setTemplate(template);
        todo.setStatus(TodoStatus.PENDING);
        entityManager.persistAndFlush(todo);

        // When
        todo.setStatus(TodoStatus.COMPLETED);
        Todo updatedTodo = entityManager.persistAndFlush(todo);

        // Then
        assertThat(updatedTodo.getCompletedAt()).isNotNull();
        assertThat(updatedTodo.getCompletedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void shouldCreateProgressWithValidData() {
        // Given
        Progress progress = new Progress();
        progress.setHire(hire);
        progress.setTemplate(template);
        progress.setTotalTasks(5);
        progress.setCompletedTasks(2);

        // When
        Progress savedProgress = entityManager.persistAndFlush(progress);

        // Then
        assertThat(savedProgress.getId()).isNotNull();
        assertThat(savedProgress.getHire()).isEqualTo(hire);
        assertThat(savedProgress.getTemplate()).isEqualTo(template);
        assertThat(savedProgress.getTotalTasks()).isEqualTo(5);
        assertThat(savedProgress.getCompletedTasks()).isEqualTo(2);
        assertThat(savedProgress.getCompletionPercentage()).isEqualTo(40.0);
        assertThat(savedProgress.getCreatedAt()).isNotNull();
        assertThat(savedProgress.getLastUpdated()).isNotNull();
    }

    @Test
    void shouldCalculateCompletionPercentageOnUpdate() {
        // Given
        Progress progress = new Progress();
        progress.setHire(hire);
        progress.setTemplate(template);
        progress.setTotalTasks(10);
        progress.setCompletedTasks(3);
        entityManager.persistAndFlush(progress);

        // When
        progress.setCompletedTasks(7);
        Progress updatedProgress = entityManager.persistAndFlush(progress);

        // Then
        assertThat(updatedProgress.getCompletionPercentage()).isEqualTo(70.0);
        assertThat(updatedProgress.getLastUpdated()).isAfter(updatedProgress.getCreatedAt());
    }

    @Test
    void shouldHandleZeroTotalTasksInProgress() {
        // Given
        Progress progress = new Progress();
        progress.setHire(hire);
        progress.setTemplate(template);
        progress.setTotalTasks(0);
        progress.setCompletedTasks(0);

        // When
        Progress savedProgress = entityManager.persistAndFlush(progress);

        // Then
        assertThat(savedProgress.getCompletionPercentage()).isEqualTo(0.0);
    }

    @Test
    void shouldEnforceUniqueConstraintOnHireAndTemplate() {
        // Given
        Progress progress1 = new Progress();
        progress1.setHire(hire);
        progress1.setTemplate(template);
        progress1.setTotalTasks(5);
        progress1.setCompletedTasks(2);
        entityManager.persistAndFlush(progress1);

        Progress progress2 = new Progress();
        progress2.setHire(hire);
        progress2.setTemplate(template);
        progress2.setTotalTasks(3);
        progress2.setCompletedTasks(1);

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(progress2))
                .isInstanceOf(Exception.class);
    }
}