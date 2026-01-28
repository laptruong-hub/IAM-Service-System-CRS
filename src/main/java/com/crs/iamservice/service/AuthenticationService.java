package com.crs.iamservice.service;

import com.crs.iamservice.dto.request.*;
import com.crs.iamservice.dto.response.AuthenticationResponse;
import com.crs.iamservice.dto.response.IntrospectResponse;
import com.crs.iamservice.dto.response.RegisterResponse;
import com.crs.iamservice.dto.response.UserResponse;
import com.crs.iamservice.entity.User;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
    RegisterResponse register(RegisterRequest request);
    IntrospectResponse introspect(IntrospectRequest request);
    UserResponse getMyProfile();
    void logout(LogoutRequest request);
    void changePassword(ChangePasswordRequest request);
}