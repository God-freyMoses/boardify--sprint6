package com.shaper.server.controller;

import com.shaper.server.model.dto.LoginRequestDTO;
import com.shaper.server.model.dto.RegisterRequestDTO;
import com.shaper.server.model.dto.UserDTO;
import com.shaper.server.model.dto.UserTokenDTO;
import com.shaper.server.service.UserService;
import com.shaper.server.system.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // REGISTER USER

    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        UserDTO userDTO = userService.register(registerRequestDTO);
        Result result = new Result(200, true, "User registered successfully!", userDTO);
        return ResponseEntity.ok(result);
    }
    
    // REGISTER HR USER (Frontend expects this endpoint)
    @PostMapping("/register/hr")
    public ResponseEntity<Result> registerHr(@RequestBody RegisterRequestDTO registerRequestDTO) {
        // Debug logging
        System.out.println("Received registration request:");
        System.out.println("Email: " + registerRequestDTO.getEmail());
        System.out.println("FirstName: " + registerRequestDTO.getFirstName());
        System.out.println("LastName: " + registerRequestDTO.getLastName());
        System.out.println("CompanyName: " + registerRequestDTO.getCompanyName());
        System.out.println("CompanyId: " + registerRequestDTO.getCompanyId());
        System.out.println("Role: " + registerRequestDTO.getRole());
        
        // Set role to HR_MANAGER for this endpoint
        registerRequestDTO.setRole("HR_MANAGER");
        UserDTO userDTO = userService.register(registerRequestDTO);
        Result result = new Result(200, true, "HR User registered successfully!", userDTO);
        return ResponseEntity.ok(result);
    }


    // REGISTER HIRE USER (Frontend expects this endpoint)
    @PostMapping("/register/hire")
    public ResponseEntity<Result> registerHire(@RequestBody RegisterRequestDTO registerRequestDTO) {
        // Debug logging
        System.out.println("Received hire registration request:");
        System.out.println("Email: " + registerRequestDTO.getEmail());
        System.out.println("FirstName: " + registerRequestDTO.getFirstName());
        System.out.println("LastName: " + registerRequestDTO.getLastName());
        System.out.println("Department: " + registerRequestDTO.getDepartmentId());
        System.out.println("Role: " + registerRequestDTO.getRole());
        
        // Set role to NEW_HIRE for this endpoint
        registerRequestDTO.setRole("NEW_HIRE");
        UserDTO userDTO = userService.register(registerRequestDTO);
        Result result = new Result(200, true, "Hire User registered successfully!", userDTO);
        return ResponseEntity.ok(result);
    }

    // GET ALL HIRES
    @GetMapping("/hires")
    public ResponseEntity<Result> getAllHires() {
        try {
            List<Map<String, Object>> hires = userService.getAllHires();
            Result result = new Result(200, true, "Hires retrieved successfully!", hires);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving hires: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    // GET HR USER BY COMPANY ID
    @GetMapping("/hr/company/{companyId}")
    public ResponseEntity<Result> getHrUserByCompanyId(@PathVariable Integer companyId) {
        try {
            Map<String, Object> hrUser = userService.getHrUserByCompanyId(companyId);
            Result result = new Result(200, true, "HR User retrieved successfully!", hrUser);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Error retrieving HR user: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    // LOGIN USER

    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        UserTokenDTO userTokenDTO = userService.login(loginRequestDTO);
        Result result = new Result(200, true, "User logged in successfully!", userTokenDTO);
        return ResponseEntity.ok(result);
    }


}
