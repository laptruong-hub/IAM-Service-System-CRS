package com.crs.iamservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Entity lưu mã OTP để reset mật khẩu.
 * Mỗi request sẽ tạo 1 bản ghi, mã OTP có thời hạn (mặc định 5 phút).
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String email;

    @Column(name = "reset_code", nullable = false, length = 6)
    String resetCode;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "is_used")
    boolean isUsed = false;

    @Column(name = "is_verified")
    boolean isVerified = false;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
