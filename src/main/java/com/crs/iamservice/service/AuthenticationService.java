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

    /**
     * Bước 1: Gửi mã OTP về email để reset mật khẩu
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Bước 2: Xác minh mã OTP
     */
    void verifyResetCode(VerifyResetCodeRequest request);

    /**
     * Bước 3: Đặt mật khẩu mới sau khi xác minh OTP thành công
     */
    void resetPassword(ResetPasswordRequest request);
}