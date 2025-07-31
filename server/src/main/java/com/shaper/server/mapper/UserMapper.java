package com.shaper.server.mapper;

import com.shaper.server.model.dto.RegisterRequestDTO;
import com.shaper.server.model.dto.UserDTO;
import com.shaper.server.model.dto.UserTokenDTO;
import com.shaper.server.model.entity.User;
import com.shaper.server.model.enums.UserRole;

public class UserMapper {

    // RegisterRequestDTO to Entity

    public static User registerRequestToEntity(RegisterRequestDTO registerRequestDTO) {
        // This method is deprecated - use specific entity creation in service layer
        throw new UnsupportedOperationException("Use specific entity creation methods in service layer");
    }



    // User entity to UserDTO

    public static UserDTO userToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId().toString());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setRole(user.getRole().name());
        userDTO.setCreatedAt(user.getCreatedAt().toString());
        
        // Set userType based on discriminator value
        if (user instanceof com.shaper.server.model.entity.HrUser) {
            userDTO.setUserType("HR");
        } else if (user instanceof com.shaper.server.model.entity.Hire) {
            userDTO.setUserType("HIRE");
        } else {
            userDTO.setUserType("USER");
        }
        
        return userDTO;
    }


    // User entity to UserTokenDTO

    public static UserTokenDTO userToUserTokenDTO(User user, String token) {
        UserTokenDTO userTokenDTO = new UserTokenDTO();
        userTokenDTO.setUser(userToUserDTO(user));
        userTokenDTO.setToken(token);
        return userTokenDTO;
    }
}
