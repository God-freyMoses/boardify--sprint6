package com.shaper.server.model.entity;

import com.shaper.server.model.enums.TaskType;
import com.shaper.server.model.enums.TemplateStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TemplateTaskRelationshipTest {

    @Autowired
    private TestEntityManager entityManager;

    private HrUser hrUser;
    private Template template;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setName("Test Company");
        entityManager.persistAndFlush(company);

        hrUser = new HrUser();
        hrUser.setEmail("hr@test.com");
        hrUser.setPassword("password123");
        hrUser.setFirstName("HR");
        hrUser.setLastName("Manager");
        hrUser.setCompany(company);
        entityManager.persistAndFlush(hrUser);

        template = new Template();
        template.setTitle("Test Template");
        template.setDescription("Test Description");
        template.setCreatedByHr(hrUser);
        entityManager.persistAndFlush(template);
    }

    @Test
    void shouldCreateTaskWithTemplateRelationship() {
        // Given
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test task description");
        task.setTaskType(TaskType.DOCUMENT);
        task.setRequiresSignature(true);
        task.setOrderIndex(1);
        task.setTemplate(template);

        // When
        Task savedTask = entityManager.persistAndFlush(task);

        // Then
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTemplate()).isEqualTo(template);
        assertThat(savedTask.getTaskType()).isEqualTo(TaskType.DOCUMENT);
        assertThat(savedTask.isRequiresSignature()).isTrue();
        assertThat(savedTask.getOrderIndex()).isEqualTo(1);
    }

    @Test
    void shouldMaintainTaskOrderInTemplate() {
        // Given
        Task task1 = createTask("Task 1", 1);
        Task task2 = createTask("Task 2", 2);
        Task task3 = createTask("Task 3", 3);

        // When
        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);
        entityManager.persistAndFlush(task3);
        entityManager.clear();

        // Then
        Template foundTemplate = entityManager.find(Template.class, template.getId());
        assertThat(foundTemplate.getTasks()).hasSize(3);
        
        // Tasks should be ordered by orderIndex
        Task[] tasksArray = foundTemplate.getTasks().toArray(new Task[0]);
        assertThat(tasksArray[0].getOrderIndex()).isLessThan(tasksArray[1].getOrderIndex());
        assertThat(tasksArray[1].getOrderIndex()).isLessThan(tasksArray[2].getOrderIndex());
    }

    @Test
    void shouldCascadeDeleteTasksWhenTemplateDeleted() {
        // Given
        Task task1 = createTask("Task 1", 1);
        Task task2 = createTask("Task 2", 2);
        
        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);
        
        Integer task1Id = task1.getId();
        Integer task2Id = task2.getId();
        Integer templateId = template.getId();
        
        entityManager.clear();

        // When
        Template foundTemplate = entityManager.find(Template.class, templateId);
        entityManager.remove(foundTemplate);
        entityManager.flush();

        // Then
        assertThat(entityManager.find(Task.class, task1Id)).isNull();
        assertThat(entityManager.find(Task.class, task2Id)).isNull();
    }

    @Test
    void shouldSetDefaultTemplateStatus() {
        // Given & When
        Template newTemplate = new Template();
        newTemplate.setTitle("New Template");
        newTemplate.setDescription("New Description");
        newTemplate.setCreatedByHr(hrUser);
        
        Template savedTemplate = entityManager.persistAndFlush(newTemplate);

        // Then
        assertThat(savedTemplate.getStatus()).isEqualTo(TemplateStatus.PENDING);
        assertThat(savedTemplate.getCreatedDate()).isNotNull();
        assertThat(savedTemplate.getUpdatedDate()).isNotNull();
    }

    private Task createTask(String title, int orderIndex) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setTaskType(TaskType.DOCUMENT);
        task.setRequiresSignature(false);
        task.setOrderIndex(orderIndex);
        task.setTemplate(template);
        return task;
    }
}