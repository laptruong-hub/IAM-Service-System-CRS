package com.crs.iamservice.repository;

import com.crs.iamservice.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Tìm mã OTP chưa sử dụng theo email và resetCode
     */
    Optional<PasswordResetToken> findByEmailAndResetCodeAndIsUsedFalse(String email, String resetCode);

    /**
     * Tìm mã OTP đã xác minh nhưng chưa sử dụng
     */
    Optional<PasswordResetToken> findByEmailAndResetCodeAndIsVerifiedTrueAndIsUsedFalse(String email, String resetCode);

    /**
     * Xóa tất cả token cũ của một email (dọn dẹp)
     */
    void deleteAllByEmail(String email);
}
