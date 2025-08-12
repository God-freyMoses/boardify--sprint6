package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.exception.UnAutororizedException;
import com.shaper.server.model.dto.LoginRequestDTO;
import com.shaper.server.model.dto.RegisterRequestDTO;
import com.shaper.server.model.dto.UserDTO;
import com.shaper.server.model.dto.UserTokenDTO;
import com.shaper.server.model.entity.Company;
import com.shaper.server.model.entity.HrUser;
import com.shaper.server.model.entity.User;
import com.shaper.server.model.enums.UserRole;
import com.shaper.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyDepartmentRepository companyDepartmentRepository;

    @Mock
    private HrUserRepository hrUserRepository;

    @Mock
    private HireRepository hireRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequestDTO hrRegisterRequest;
    private LoginRequestDTO loginRequest;
    private Company testCompany;
    private HrUser testHrUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        hrRegisterRequest = new RegisterRequestDTO();
        hrRegisterRequest.setEmail("hr@example.com");
        hrRegisterRequest.setPassword("password123");
        hrRegisterRequest.setFirstName("John");
        hrRegisterRequest.setLastName("Doe");
        hrRegisterRequest.setRole("HR_MANAGER");
        hrRegisterRequest.setCompanyName("Test Company");

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("hr@example.com");
        loginRequest.setPassword("password123");

        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");

        testHrUser = new HrUser();
        testHrUser.setId(UUID.randomUUID());
        testHrUser.setEmail("hr@example.com");
        testHrUser.setPassword("encodedPassword");
        testHrUser.setFirstName("John");
        testHrUser.setLastName("Doe");
        testHrUser.setRole(UserRole.HR_MANAGER);
        testHrUser.setCompany(testCompany);
        testHrUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldRegisterHrUserWithNewCompany() {
        // Given
        when(userRepository.existsByEmail(hrRegisterRequest.getEmail())).thenReturn(false);
        when(companyRepository.findByName(hrRegisterRequest.getCompanyName())).thenReturn(null);
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);
        when(passwordEncoder.encode(hrRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(HrUser.class))).thenReturn(testHrUser);

        // When
        UserDTO result = userService.register(hrRegisterRequest);

        // Then
        assertNotNull(result);
        assertEquals(hrRegisterRequest.getEmail(), result.getEmail());
        assertEquals(hrRegisterRequest.getFirstName(), result.getFirstName());
        assertEquals(hrRegisterRequest.getLastName(), result.getLastName());

        verify(companyRepository).save(any(Company.class));
        verify(userRepository).save(any(HrUser.class));
        verify(passwordEncoder).encode(hrRegisterRequest.getPassword());
    }

    @Test
    void shouldRegisterHrUserWithExistingCompany() {
        // Given
        when(userRepository.existsByEmail(hrRegisterRequest.getEmail())).thenReturn(false);
        when(companyRepository.findByName(hrRegisterRequest.getCompanyName())).thenReturn(testCompany);
        when(passwordEncoder.encode(hrRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(HrUser.class))).thenReturn(testHrUser);

        // When
        UserDTO result = userService.register(hrRegisterRequest);

        // Then
        assertNotNull(result);
        assertEquals(hrRegisterRequest.getEmail(), result.getEmail());

        verify(companyRepository, never()).save(any(Company.class)); // Should not create new company
        verify(userRepository).save(any(HrUser.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingUser() {
        // Given
        when(userRepository.existsByEmail(hrRegisterRequest.getEmail())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.register(hrRegisterRequest));
        
        assertEquals("User with this email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(testHrUser);
        when(passwordEncoder.matches(loginRequest.getPassword(), testHrUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testHrUser)).thenReturn("jwt-token");

        // When
        UserTokenDTO result = userService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertNotNull(result.getUser());
        assertEquals(testHrUser.getEmail(), result.getUser().getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(null);

        // When & Then
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
            () -> userService.login(loginRequest));
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(testHrUser);
        when(passwordEncoder.matches(loginRequest.getPassword(), testHrUser.getPassword())).thenReturn(false);

        // When & Then
        UnAutororizedException exception = assertThrows(UnAutororizedException.class,
            () -> userService.login(loginRequest));
        
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // Given
        String refreshToken = "valid-refresh-token";
        when(jwtService.extractUserName(refreshToken)).thenReturn(testHrUser.getEmail());
        when(userRepository.findByEmail(testHrUser.getEmail())).thenReturn(testHrUser);
        when(jwtService.isTokenValid(eq(refreshToken), any())).thenReturn(true);
        when(jwtService.generateToken(testHrUser)).thenReturn("new-jwt-token");

        // When
        UserTokenDTO result = userService.refreshToken(refreshToken);

        // Then
        assertNotNull(result);
        assertEquals("new-jwt-token", result.getToken());
        assertNotNull(result.getUser());
        assertEquals(testHrUser.getEmail(), result.getUser().getEmail());
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
        // Given
        String invalidRefreshToken = "invalid-refresh-token";
        when(jwtService.extractUserName(invalidRefreshToken)).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        UnAutororizedException exception = assertThrows(UnAutororizedException.class,
            () -> userService.refreshToken(invalidRefreshToken));
        
        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCompanyNameAndIdAreBothMissing() {
        // Given
        hrRegisterRequest.setCompanyName(null);
        hrRegisterRequest.setCompanyId(null);
        when(userRepository.existsByEmail(hrRegisterRequest.getEmail())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.register(hrRegisterRequest));
        
        assertEquals("Either companyId or companyName must be provided", exception.getMessage());
    }

    @Test
    void shouldRegisterHrUserWithCompanyId() {
        // Given
        hrRegisterRequest.setCompanyName(null);
        hrRegisterRequest.setCompanyId("1");
        when(userRepository.existsByEmail(hrRegisterRequest.getEmail())).thenReturn(false);
        when(companyRepository.findById(1)).thenReturn(Optional.of(testCompany));
        when(passwordEncoder.encode(hrRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(HrUser.class))).thenReturn(testHrUser);

        // When
        UserDTO result = userService.register(hrRegisterRequest);

        // Then
        assertNotNull(result);
        assertEquals(hrRegisterRequest.getEmail(), result.getEmail());
        verify(companyRepository).findById(1);
        verify(userRepository).save(any(HrUser.class));
    }
}