package com.shaper.server.controller;

import com.shaper.server.model.dto.LoginRequestDTO;
import com.shaper.server.model.dto.RegisterRequestDTO;
import com.shaper.server.model.dto.UserDTO;
import com.shaper.server.model.dto.UserTokenDTO;
import com.shaper.server.service.UserService;
import com.shaper.server.system.Result;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Result> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        try {
            // Set role to HR_MANAGER for auth registration endpoint
            registerRequestDTO.setRole("HR_MANAGER");
            UserDTO userDTO = userService.register(registerRequestDTO);
            Result result = new Result(201, true, "HR Manager registered successfully!", userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            Result result = new Result(400, false, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Result> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            UserTokenDTO userTokenDTO = userService.login(loginRequestDTO);
            Result result = new Result(200, true, "Login successful!", userTokenDTO);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Result result = new Result(401, false, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Result> refresh(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Result result = new Result(400, false, "Invalid authorization header");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            String refreshToken = authHeader.substring(7);
            UserTokenDTO userTokenDTO = userService.refreshToken(refreshToken);
            Result result = new Result(200, true, "Token refreshed successfully!", userTokenDTO);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Result result = new Result(401, false, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        } catch (Exception e) {
            Result result = new Result(500, false, "Token refresh failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Result> logout() {
        // For JWT, logout is handled client-side by removing the token
        // In a production system, you might want to maintain a blacklist of tokens
        Result result = new Result(200, true, "Logout successful!");
        return ResponseEntity.ok(result);
    }
}