package com.crs.iamservice.controller;

import com.crs.iamservice.config.UserPrincipal;
import com.crs.iamservice.dto.request.*;
import com.crs.iamservice.dto.response.AuthenticationResponse;
import com.crs.iamservice.dto.response.IntrospectResponse;
import com.crs.iamservice.dto.response.RegisterResponse;
import com.crs.iamservice.dto.response.UserResponse;
import com.crs.iamservice.entity.RefreshToken;
import com.crs.iamservice.service.AuthenticationService;
import com.crs.iamservice.service.JwtService;
import com.crs.iamservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService; // Tiêm Interface
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        // Gọi Service xử lý logic và trả về Token
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> introspect(
            @RequestBody IntrospectRequest request
    ) {
        return ResponseEntity.ok(authenticationService.introspect(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(authenticationService.getMyProfile());
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    var user = refreshToken.getUser();
                    // Tạo Access Token mới từ thông tin User
                    String accessToken = jwtService.generateToken(
                            UserPrincipal.create(user)
                    );

                    return ResponseEntity.ok(
                            AuthenticationResponse.builder()
                                    .accessToken(accessToken)
                                    .refreshToken(request.refreshToken())
                                    .email(user.getEmail())
                                    .role(user.getRole().getName())
                                    .build()
                    );
                })
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại!"));
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok("Đăng xuất thành công");
    }
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}