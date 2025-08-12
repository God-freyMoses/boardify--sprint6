package com.shaper.server.service.impl;

import com.shaper.server.CustomUserDetails;
import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.exception.UnAutororizedException;
import com.shaper.server.mapper.UserMapper;
import com.shaper.server.model.dto.LoginRequestDTO;
import com.shaper.server.model.dto.RegisterRequestDTO;
import com.shaper.server.model.dto.UserDTO;
import com.shaper.server.model.dto.UserTokenDTO;
import com.shaper.server.model.entity.Company;
import com.shaper.server.model.entity.CompanyDepartment;
import com.shaper.server.model.entity.Hire;
import com.shaper.server.model.entity.HrUser;
import com.shaper.server.model.entity.User;
import com.shaper.server.model.enums.UserRole;
import com.shaper.server.repository.CompanyDepartmentRepository;
import com.shaper.server.repository.CompanyRepository;
import com.shaper.server.repository.HireRepository;
import com.shaper.server.repository.HrUserRepository;
import com.shaper.server.repository.UserRepository;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import com.shaper.server.service.UserService;
import com.shaper.server.service.impl.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyDepartmentRepository companyDepartmentRepository;
    private final HrUserRepository hrUserRepository;
    private final HireRepository hireRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, 
                          CompanyRepository companyRepository,
                          CompanyDepartmentRepository companyDepartmentRepository,
                          HrUserRepository hrUserRepository,
                          HireRepository hireRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.companyDepartmentRepository = companyDepartmentRepository;
        this.hrUserRepository = hrUserRepository;
        this.hireRepository = hireRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }



    @Override
    public UserDTO register(RegisterRequestDTO registerRequestDTO) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create user based on role
        User user;
        if ("HR_MANAGER".equals(registerRequestDTO.getRole())) {
            user = createHrUser(registerRequestDTO);
        } else if ("NEW_HIRE".equals(registerRequestDTO.getRole())) {
            user = createHire(registerRequestDTO);
        } else {
            throw new RuntimeException("Only HR_MANAGER and NEW_HIRE registration are currently supported");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        
        // Save user
        User savedUser = userRepository.save(user);
        
        return UserMapper.userToUserDTO(savedUser);
    }

    @Override
    public UserTokenDTO login(LoginRequestDTO loginRequestDTO) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequestDTO.getEmail());
        if (user == null) {
            throw new DataNotFoundException("User not found");
        }

        // Check password
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new UnAutororizedException("Invalid credentials");
        }

        // Generate token
        String token = jwtService.generateToken(user);
        
        return UserMapper.userToUserTokenDTO(user, token);
    }

    @Override
    public UserTokenDTO refreshToken(String refreshToken) {
        try {
            // Extract username from refresh token
            String username = jwtService.extractUserName(refreshToken);
            
            // Find user
            User user = userRepository.findByEmail(username);
            if (user == null) {
                throw new DataNotFoundException("User not found");
            }

            // Validate refresh token
            CustomUserDetails userDetails = new CustomUserDetails(user);
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new UnAutororizedException("Invalid refresh token");
            }

            // Generate new access token
            String newToken = jwtService.generateToken(user);
            
            return UserMapper.userToUserTokenDTO(user, newToken);
        } catch (Exception e) {
            throw new UnAutororizedException("Invalid refresh token");
        }
    }

    private HrUser createHrUser(RegisterRequestDTO registerRequestDTO) {
        HrUser hrUser = new HrUser();
        hrUser.setEmail(registerRequestDTO.getEmail());
        hrUser.setFirstName(registerRequestDTO.getFirstName());
        hrUser.setLastName(registerRequestDTO.getLastName());
        hrUser.setRole(UserRole.HR_MANAGER);
        
        // Handle company
        Company company;
        if (registerRequestDTO.getCompanyId() != null) {
            // Use existing company
            company = companyRepository.findById(Integer.valueOf(registerRequestDTO.getCompanyId()))
                    .orElseThrow(() -> new DataNotFoundException("Company not found"));
        } else if (registerRequestDTO.getCompanyName() != null) {
            // Check if company already exists
            company = companyRepository.findByName(registerRequestDTO.getCompanyName());
            if (company == null) {
                // Create new company if it doesn't exist
                company = new Company();
                company.setName(registerRequestDTO.getCompanyName());
                company = companyRepository.save(company);
            }
            // If company exists, use the existing one
        } else {
            throw new RuntimeException("Either companyId or companyName must be provided");
        }
        
        hrUser.setCompany(company);
        return hrUser;
    }

    private Hire createHire(RegisterRequestDTO registerRequestDTO) {
        // Find the department by ID
        CompanyDepartment department = companyDepartmentRepository.findById(Integer.valueOf(registerRequestDTO.getDepartmentId()))
                .orElse(null);
        if (department == null) {
            throw new RuntimeException("Department not found with ID: " + registerRequestDTO.getDepartmentId());
        }

        // Generate a random password for the hire
        String generatedPassword = generateRandomPassword();
        
        // Create hire user
        Hire hire = new Hire();
        hire.setEmail(registerRequestDTO.getEmail());
        hire.setPassword(generatedPassword); // Will be encoded in the register method
        hire.setFirstName(registerRequestDTO.getFirstName());
        hire.setLastName(registerRequestDTO.getLastName());
        hire.setGender(registerRequestDTO.getGender());
        hire.setTitle(registerRequestDTO.getTitle());
        hire.setPictureUrl(registerRequestDTO.getPictureUrl());
        hire.setDepartment(department);
        
        // Find an HR user from the same company to associate with
        List<HrUser> hrUsers = hrUserRepository.findByCompany_Id(department.getCompany().getId());
        HrUser hrUser = hrUsers.isEmpty() ? null : hrUsers.get(0);
        if (hrUser == null) {
            throw new RuntimeException("No HR user found for company: " + department.getCompany().getName());
        }
        
        hire.setRegisteredByHr(hrUser);
        
        return hire;
    }
    
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    @Override
    public List<Map<String, Object>> getAllHires() {
        List<Hire> hires = hireRepository.findAll();
        return hires.stream()
            .map(hire -> {
                Map<String, Object> hireMap = new HashMap<>();
                hireMap.put("id", hire.getId());
                hireMap.put("firstName", hire.getFirstName());
                hireMap.put("lastName", hire.getLastName());
                hireMap.put("email", hire.getEmail());
                hireMap.put("title", hire.getTitle());
                hireMap.put("gender", hire.getGender());
                hireMap.put("pictureUrl", hire.getPictureUrl());
                hireMap.put("createdAt", hire.getCreatedAt());
                
                // Department info
                if (hire.getDepartment() != null) {
                    hireMap.put("departmentId", hire.getDepartment().getId());
                    hireMap.put("departmentName", hire.getDepartment().getName());
                }
                
                // HR Manager info
                if (hire.getRegisteredByHr() != null) {
                    hireMap.put("hrManagerName", hire.getRegisteredByHr().getFirstName() + " " + hire.getRegisteredByHr().getLastName());
                }
                
                return hireMap;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getHrUserByCompanyId(Integer companyId) {
        List<HrUser> hrUsers = hrUserRepository.findByCompany_Id(companyId);
        HrUser hrUser = hrUsers.isEmpty() ? null : hrUsers.get(0);
        if (hrUser == null) {
            throw new RuntimeException("No HR user found for company ID: " + companyId);
        }
        
        Map<String, Object> hrUserMap = new HashMap<>();
        hrUserMap.put("id", hrUser.getId());
        hrUserMap.put("firstName", hrUser.getFirstName());
        hrUserMap.put("lastName", hrUser.getLastName());
        hrUserMap.put("email", hrUser.getEmail());
        hrUserMap.put("role", hrUser.getRole());
        
        // Company info
        if (hrUser.getCompany() != null) {
            hrUserMap.put("companyId", hrUser.getCompany().getId());
            hrUserMap.put("companyName", hrUser.getCompany().getName());
        }
        
        return hrUserMap;
    }



    @Override
    public User findById(UUID userId) {
        // TODO Auto-generated method stub
        return userRepository.findById(userId).orElse(null);
    }

}