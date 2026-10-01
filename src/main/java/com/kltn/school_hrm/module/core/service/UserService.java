package com.kltn.school_hrm.module.core.service;

import java.util.List;

import com.kltn.school_hrm.module.core.dto.request.UserRegisterRequest;
import com.kltn.school_hrm.module.core.dto.response.UserResponse;

public interface UserService {
	UserResponse registerUser(UserRegisterRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserRegisterRequest request);
    void deleteUser(Long id);
}
