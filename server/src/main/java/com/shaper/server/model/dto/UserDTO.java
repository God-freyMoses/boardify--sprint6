package com.shaper.server.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String createdAt;
    private String userType;

}
