package com.shaper.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
    private String companyName;
    
    // For HR registration only
    private String companyId;
    
    // For Hire registration only
    private String gender;
    private String title;
    private String pictureUrl;
    private String departmentId;
}
