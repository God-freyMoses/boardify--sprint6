package com.shaper.server.model.entity;

import com.shaper.server.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setName("Test Company");
        entityManager.persistAndFlush(testCompany);
    }

    @Test
    void shouldCreateHrUserWithValidData() {
        // Given
        HrUser hrUser = new HrUser();
        hrUser.setEmail("hr@test.com");
        hrUser.setPassword("password123");
        hrUser.setFirstName("John");
        hrUser.setLastName("Doe");
        hrUser.setCompany(testCompany);

        // When
        HrUser savedUser = entityManager.persistAndFlush(hrUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("hr@test.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.HR_MANAGER);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void shouldCreateHireUserWithValidData() {
        // Given
        HrUser hrUser = new HrUser();
        hrUser.setEmail("hr@test.com");
        hrUser.setPassword("password123");
        hrUser.setFirstName("HR");
        hrUser.setLastName("Manager");
        hrUser.setCompany(testCompany);
        entityManager.persistAndFlush(hrUser);

        CompanyDepartment department = new CompanyDepartment();
        department.setName("Engineering");
        department.setCompany(testCompany);
        department.setCreatedByHr(hrUser);
        entityManager.persistAndFlush(department);

        Hire hire = new Hire();
        hire.setEmail("hire@test.com");
        hire.setPassword("password123");
        hire.setFirstName("Jane");
        hire.setLastName("Smith");
        hire.setTitle("Software Engineer");
        hire.setRegisteredByHr(hrUser);
        hire.setDepartment(department);

        // When
        Hire savedHire = entityManager.persistAndFlush(hire);

        // Then
        assertThat(savedHire.getId()).isNotNull();
        assertThat(savedHire.getEmail()).isEqualTo("hire@test.com");
        assertThat(savedHire.getRole()).isEqualTo(UserRole.NEW_HIRE);
        assertThat(savedHire.getTitle()).isEqualTo("Software Engineer");
        assertThat(savedHire.getRegisteredByHr()).isEqualTo(hrUser);
        assertThat(savedHire.getDepartment()).isEqualTo(department);
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        // Given
        HrUser hrUser = new HrUser();
        hrUser.setPassword("password123");
        hrUser.setFirstName("John");
        hrUser.setLastName("Doe");
        hrUser.setCompany(testCompany);

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(hrUser))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void shouldFailWhenEmailIsDuplicate() {
        // Given
        HrUser hrUser1 = new HrUser();
        hrUser1.setEmail("duplicate@test.com");
        hrUser1.setPassword("password123");
        hrUser1.setFirstName("John");
        hrUser1.setLastName("Doe");
        hrUser1.setCompany(testCompany);
        entityManager.persistAndFlush(hrUser1);

        HrUser hrUser2 = new HrUser();
        hrUser2.setEmail("duplicate@test.com");
        hrUser2.setPassword("password456");
        hrUser2.setFirstName("Jane");
        hrUser2.setLastName("Smith");
        hrUser2.setCompany(testCompany);

        // When & Then
        assertThatThrownBy(() -> entityManager.persistAndFlush(hrUser2))
                .isInstanceOf(Exception.class);
    }
}