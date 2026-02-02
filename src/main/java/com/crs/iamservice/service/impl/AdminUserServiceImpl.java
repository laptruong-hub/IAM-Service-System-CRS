package com.crs.iamservice.service.impl;

import com.crs.iamservice.dto.request.AdminPasswordResetRequest;
import com.crs.iamservice.dto.request.AdminUserRequest;
import com.crs.iamservice.dto.request.AdminUserUpdateRequest;
import com.crs.iamservice.dto.request.UserSearchRequest;
import com.crs.iamservice.dto.response.AdminUserResponse;
import com.crs.iamservice.dto.response.PageResponse;
import com.crs.iamservice.entity.Role;
import com.crs.iamservice.entity.User;
import com.crs.iamservice.exception.DuplicateResourceException;
import com.crs.iamservice.exception.ResourceNotFoundException;
import com.crs.iamservice.repository.RoleRepository;
import com.crs.iamservice.repository.UserRepository;
import com.crs.iamservice.service.AdminUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getAllUsers(Pageable pageable) {
        log.info("Admin đang lấy danh sách tất cả users, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> userPage = userRepository.findAll(pageable);

        List<AdminUserResponse> responses = userPage.getContent().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserResponse>builder()
                .content(responses)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .isFirst(userPage.isFirst())
                .isLast(userPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> searchUsers(UserSearchRequest request) {
        log.info("Admin đang tìm kiếm users với keyword: {}, roleId: {}, isActive: {}",
                request.keyword(), request.roleId(), request.isActive());

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        // Tìm kiếm keyword trong email, fullName, phone
        if (request.keyword() != null && !request.keyword().trim().isEmpty()) {
            String keyword = "%" + request.keyword().toLowerCase() + "%";
            Predicate emailLike = cb.like(cb.lower(user.get("email")), keyword);
            Predicate nameLike = cb.like(cb.lower(user.get("fullName")), keyword);
            Predicate phoneLike = cb.like(cb.lower(user.get("phone")), keyword);
            predicates.add(cb.or(emailLike, nameLike, phoneLike));
        }

        // Lọc theo role
        if (request.roleId() != null) {
            predicates.add(cb.equal(user.get("role").get("id"), request.roleId()));
        }

        // Lọc theo trạng thái active
        if (request.isActive() != null) {
            predicates.add(cb.equal(user.get("isActive"), request.isActive()));
        }

        // Lọc theo trạng thái deleted
        if (request.isDeleted() != null) {
            predicates.add(cb.equal(user.get("isDeleted"), request.isDeleted()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(user.get("createdAt")));

        // Execute query with pagination
        Pageable pageable = PageRequest.of(request.page(), request.size());
        List<User> users = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count total
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = new ArrayList<>();
        if (request.keyword() != null && !request.keyword().trim().isEmpty()) {
            String keyword = "%" + request.keyword().toLowerCase() + "%";
            countPredicates.add(cb.or(
                    cb.like(cb.lower(countRoot.get("email")), keyword),
                    cb.like(cb.lower(countRoot.get("fullName")), keyword),
                    cb.like(cb.lower(countRoot.get("phone")), keyword)));
        }
        if (request.roleId() != null) {
            countPredicates.add(cb.equal(countRoot.get("role").get("id"), request.roleId()));
        }
        if (request.isActive() != null) {
            countPredicates.add(cb.equal(countRoot.get("isActive"), request.isActive()));
        }
        if (request.isDeleted() != null) {
            countPredicates.add(cb.equal(countRoot.get("isDeleted"), request.isDeleted()));
        }
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<AdminUserResponse> responses = users.stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / request.size());

        return PageResponse.<AdminUserResponse>builder()
                .content(responses)
                .pageNumber(request.page())
                .pageSize(request.size())
                .totalElements(total)
                .totalPages(totalPages)
                .isFirst(request.page() == 0)
                .isLast(request.page() >= totalPages - 1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(String userId) {
        log.info("Admin đang xem chi tiết user: {}", userId);

        User user = userRepository.findByIdWithRoleAndPermissions(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        return mapToAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse createUser(AdminUserRequest request) {
        log.info("Admin đang tạo user mới với email: {}", request.email());

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email đã tồn tại: " + request.email());
        }

        // Kiểm tra phone nếu có
        if (request.phone() != null && !request.phone().trim().isEmpty()
                && userRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException("Số điện thoại đã tồn tại: " + request.phone());
        }

        // Lấy role
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với ID: " + request.roleId()));

        // Tạo user mới
        User user = User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .gender(request.gender())
                .dob(request.dob())
                .role(role)
                .isActive(request.isActive() != null ? request.isActive() : true)
                .isDeleted(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Admin đã tạo user mới thành công: {}", savedUser.getUserId());

        return mapToAdminUserResponse(savedUser);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUser(String userId, AdminUserUpdateRequest request) {
        log.info("Admin đang cập nhật user: {}", userId);

        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        // Cập nhật email nếu có
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("Email đã tồn tại: " + request.email());
            }
            user.setEmail(request.email());
        }

        // Cập nhật các trường khác
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }

        if (request.phone() != null) {
            if (!request.phone().equals(user.getPhone()) && userRepository.existsByPhone(request.phone())) {
                throw new DuplicateResourceException("Số điện thoại đã tồn tại: " + request.phone());
            }
            user.setPhone(request.phone());
        }

        if (request.gender() != null) {
            user.setGender(request.gender());
        }

        if (request.dob() != null) {
            user.setDob(request.dob());
        }

        if (request.roleId() != null) {
            Role role = roleRepository.findById(request.roleId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy role với ID: " + request.roleId()));
            user.setRole(role);
        }

        if (request.isActive() != null) {
            user.setActive(request.isActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("Admin đã cập nhật user thành công: {}", userId);

        return mapToAdminUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        log.info("Đang xóa tài khoản: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + userId));

        user.setDeleted(true);
        user.setActive(false); // Cũng vô hiệu hóa khi xóa
        userRepository.save(user);

        log.info("Xóa tài khoản thành công: {}", userId);
    }

    @Override
    @Transactional
    public AdminUserResponse activateUser(String userId) {
        log.info("Admin đang kích hoạt user: {}", userId);

        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        user.setActive(true);
        User activatedUser = userRepository.save(user);

        log.info("Admin đã kích hoạt user thành công: {}", userId);
        return mapToAdminUserResponse(activatedUser);
    }

    @Override
    @Transactional
    public AdminUserResponse deactivateUser(String userId) {
        log.info("Admin đang vô hiệu hóa user: {}", userId);

        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        user.setActive(false);
        User deactivatedUser = userRepository.save(user);

        log.info("Admin đã vô hiệu hóa user thành công: {}", userId);
        return mapToAdminUserResponse(deactivatedUser);
    }

    @Override
    @Transactional
    public void resetUserPassword(String userId, AdminPasswordResetRequest request) {
        log.info("Admin đang reset mật khẩu cho user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + userId));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Admin đã reset mật khẩu cho user thành công: {}", userId);
    }

    // Helper method để map User entity sang AdminUserResponse
    private AdminUserResponse mapToAdminUserResponse(User user) {
        AdminUserResponse.RoleInfo roleInfo = null;
        if (user.getRole() != null) {
            roleInfo = AdminUserResponse.RoleInfo.builder()
                    .id(user.getRole().getId())
                    .name(user.getRole().getName())
                    .permissions(user.getRole().getPermissions() != null
                            ? user.getRole().getPermissions().stream()
                                    .map(p -> p.getId())
                                    .collect(Collectors.toSet())
                            : null)
                    .build();
        }

        return AdminUserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .gender(user.getGender())
                .dob(user.getDob())
                .isActive(user.isActive())
                .isDeleted(user.isDeleted())
                .createdAt(user.getCreatedAt())
                .role(roleInfo)
                .build();
    }
}
