package com.shaper.server.service;

import com.shaper.server.model.dto.LoginRequestDTO;
import com.shaper.server.model.dto.RegisterRequestDTO;
import com.shaper.server.model.dto.UserDTO;
import com.shaper.server.model.dto.UserTokenDTO;
import com.shaper.server.model.entity.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {

    // REGISTER
    UserDTO register(RegisterRequestDTO registerRequestDTO);

    // LOGIN
    UserTokenDTO login(LoginRequestDTO loginRequestDTO);

    // REFRESH TOKEN
    UserTokenDTO refreshToken(String refreshToken);

    // GET ALL HIRES
    List<Map<String, Object>> getAllHires();

    // GET HR USER BY COMPANY ID
    Map<String, Object> getHrUserByCompanyId(Integer companyId);

    User findById(UUID userId);
}
