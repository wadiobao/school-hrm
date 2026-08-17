package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.UserRegisterRequest;
import com.kltn.school_hrm.dto.response.UserResponse;

public interface UserService {
	UserResponse registerUser(UserRegisterRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
}
