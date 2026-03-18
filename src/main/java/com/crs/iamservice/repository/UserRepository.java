package com.crs.iamservice.repository;

import com.crs.iamservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Tìm user theo email để đăng nhập
    Optional<User> findByEmail(String email);

    // Kiểm tra tồn tại để validate khi đăng ký
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.role r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    // Admin queries for user management
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.userId = :userId")
    Optional<User> findByIdWithRole(@Param("userId") String userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.permissions WHERE u.userId = :userId")
    Optional<User> findByIdWithRoleAndPermissions(@Param("userId") String userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role r WHERE UPPER(r.name) = UPPER(:roleName) AND u.isActive = true AND u.isDeleted = false")
    List<User> findActiveUsersByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false")
    long countTotalActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE UPPER(u.role.name) = 'CUSTOMER' AND u.isDeleted = false")
    long countTotalCustomers();

    @Query("SELECT COUNT(u) FROM User u WHERE UPPER(u.role.name) = 'CUSTOMER' AND u.isDeleted = false AND u.createdAt >= :since")
    long countNewCustomersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE UPPER(u.role.name) = 'DRIVER' AND u.isDeleted = false")
    long countTotalDrivers();
}