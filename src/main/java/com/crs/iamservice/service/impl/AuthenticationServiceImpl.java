package com.crs.iamservice.service.impl;

import com.crs.iamservice.config.UserPrincipal;
import com.crs.iamservice.dto.request.*;
import com.crs.iamservice.dto.response.AuthenticationResponse;
import com.crs.iamservice.dto.response.IntrospectResponse;
import com.crs.iamservice.dto.response.RegisterResponse;
import com.crs.iamservice.dto.response.UserResponse;
import com.crs.iamservice.entity.PasswordHistory;
import com.crs.iamservice.dto.request.ChangePasswordRequest;
import com.crs.iamservice.event.UserRegistrationEvent;
import com.crs.iamservice.repository.PasswordHistoryRepository;
import com.crs.iamservice.entity.RefreshToken;
import com.crs.iamservice.entity.User;
import com.crs.iamservice.repository.RoleRepository;
import com.crs.iamservice.repository.UserRepository;
import com.crs.iamservice.service.AuthenticationService;
import com.crs.iamservice.service.JwtService;
import com.crs.iamservice.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Inject Interface
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // 2. Lấy Role mặc định là CUSTOMER (đã khởi tạo trong DataInitializer)
        var defaultRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy vai trò khách hàng"));

        // 3. Tạo Entity User mới
        var user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(defaultRole)
                .isActive(true) // Sử dụng @Builder.Default đã sửa ở bước trước
                .build();

        userRepository.save(user);

        // 4. Publish event để gửi email chào mừng (async - không block)
        eventPublisher.publishEvent(new UserRegistrationEvent(this, user.getEmail(), user.getFullName()));

        return RegisterResponse.builder()
                .message("Đăng ký tài khoản thành công!")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtService.generateToken(userPrincipal);

        var refreshTokenEntity = refreshTokenService.createRefreshToken(user.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenEntity.getToken())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.token();
        boolean isValid = true;

        try {
            // Kiểm tra Token bằng JwtService (kiểm tra hết hạn, chữ ký...)
            // Ở đây ta chỉ cần kiểm tra username có trích xuất được không và token chưa hết hạn
            String username = jwtService.extractUsername(token);

            // Bạn có thể bổ sung kiểm tra DB nếu muốn chắc chắn User vẫn tồn tại/Active
            if (username == null || username.isEmpty()) {
                isValid = false;
            }
        } catch (Exception e) {
            // Nếu có bất kỳ lỗi nào (Expired, Malformed, SignatureException...)
            isValid = false;
        }

        return IntrospectResponse.builder()
                .isValid(isValid)
                .build();
    }

    @Override
    public UserResponse getMyProfile() {
        // 1. Lấy email (username) từ SecurityContext do JwtAuthenticationFilter đã xác thực trước đó
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 2. Tìm User trong DB
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

        // 3. Map sang DTO trả về
        return UserResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .isActive(user.isActive())
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        // Tìm và xóa Refresh Token trong Database
        refreshTokenService.deleteByToken(request.refreshToken());
        // Xóa SecurityContext để đảm bảo phiên làm việc hiện tại bị ngắt hoàn toàn
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // Lấy thông tin User đang đăng nhập từ SecurityContext
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra mật khẩu cũ có khớp không
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }
        // KIỂM TRA MẬT KHẨU MỚI CÓ TRÙNG 3 LẦN GẦN NHẤT KHÔNG
        // Kiểm tra trực tiếp với mật khẩu hiện tại
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        // Lấy 3 mật khẩu cũ nhất từ lịch sử
        List<PasswordHistory> oldPasswords = passwordHistoryRepository.findTopNByUser(user, PageRequest.of(0, 3));

        for (PasswordHistory history : oldPasswords) {
            if (passwordEncoder.matches(request.newPassword(), history.getPasswordHash())) {
                throw new RuntimeException("Bạn không được sử dụng lại mật khẩu trong 3 lần gần nhất!");
            }
        }
        // Lưu mật khẩu hiện tại vào lịch sử TRƯỚC khi thay đổi
        PasswordHistory history = PasswordHistory.builder()
                .user(user)
                .passwordHash(user.getPasswordHash())
                .build();
        passwordHistoryRepository.save(history);

        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        //  Xóa tất cả Refresh Tokens để bắt buộc đăng nhập lại trên mọi thiết bị
        refreshTokenService.deleteByToken(user.getUserId());
    }


}