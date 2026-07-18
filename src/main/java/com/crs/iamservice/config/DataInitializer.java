package com.crs.iamservice.config;

import com.crs.iamservice.entity.Permission;
import com.crs.iamservice.entity.Role;
import com.crs.iamservice.entity.User;
import com.crs.iamservice.repository.PermissionRepository;
import com.crs.iamservice.repository.RoleRepository;
import com.crs.iamservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder; // Bạn cần Bean này trong SecurityConfig

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu mẫu cho IAM Service...");

            // 1. Tạo Permissions (Quyền hạn)
            Permission pCreateBooking = createPermission("CREATE_BOOKING", "Quyền tạo đơn đặt xe");
            Permission pViewVehicle = createPermission("VIEW_VEHICLE", "Quyền xem danh sách xe");
            Permission pManageUsers = createPermission("MANAGE_USERS", "Quyền quản lý người dùng");
            Permission pManageFleet = createPermission("MANAGE_FLEET", "Quyền quản lý đội xe");
            Permission pViewTrip = createPermission("VIEW_TRIP_DETAILS", "Quyền xem chi tiết chuyến đi");
            Permission pUpdateTrip = createPermission("UPDATE_TRIP_STATUS", "Quyền cập nhật trạng thái chuyến đi");
            Permission pReportIssue = createPermission("REPORT_CAR_ISSUE", "Quyền báo cáo sự cố xe");
            Permission pManagePermissions = createPermission("MANAGE_PERMISSIONS", "Quyền quản lý quyền hạn");
            Permission pManageRoles = createPermission("MANAGE_ROLES", "Quyền quản lý vai trò");


            // 2. Tạo Roles (Vai trò) và gán Permission
            // Role CUSTOMER
            Set<Permission> customerPerms = new HashSet<>();
            customerPerms.add(pCreateBooking);
            customerPerms.add(pViewVehicle);
            Role roleCustomer = createRole("CUSTOMER", customerPerms);

            // Role STAFF
            Set<Permission> staffPerms = new HashSet<>();
            staffPerms.add(pViewVehicle);
            staffPerms.add(pCreateBooking); // Staff có thể đặt xe giùm khách
            Role roleStaff = createRole("STAFF", staffPerms);

            // Role ADMIN (Full quyền)
            Set<Permission> adminPerms = new HashSet<>();
            adminPerms.add(pCreateBooking);
            adminPerms.add(pViewVehicle);
            adminPerms.add(pManageUsers);
            adminPerms.add(pManageFleet);
            adminPerms.add(pViewTrip);
            adminPerms.add(pUpdateTrip);
            adminPerms.add(pManageRoles);
            adminPerms.add(pManagePermissions);
            Role roleAdmin = createRole("ADMIN", adminPerms);

            // Role DRIVER
            Set<Permission> driverPerms = new HashSet<>();
            driverPerms.add(pViewVehicle);    // Để tài xế biết mình đang lái xe nào
            driverPerms.add(pViewTrip);       // Để xem lộ trình
            driverPerms.add(pUpdateTrip);     // Để cập nhật trạng thái chuyến đi
            driverPerms.add(pReportIssue);    // Để báo cáo hỏng hóc
            Role roleDriver = createRole("DRIVER", driverPerms);

            // 3. Tạo User Admin mặc định
            if (!userRepository.existsByEmail("admin@rental.com")) {
                User admin = User.builder()
                        .email("admin@rental.com")
                        .fullName("Super Administrator")
                        .passwordHash(passwordEncoder.encode("admin123")) // Mật khẩu mặc định
                        .role(roleAdmin)
                        .isActive(true)
                        .build();
                userRepository.save(admin);
                log.info("Đã tạo tài khoản Admin mặc định: admin@rental.com / admin123");
            }

            // 4. Tạo User Staff
            if (!userRepository.existsByEmail("staff@gmail.com")) {
                User staff = User.builder()
                        .email("staff@gmail.com")
                        .fullName("Staff Account")
                        .passwordHash(passwordEncoder.encode("staff123"))
                        .role(roleStaff)
                        .isActive(true)
                        .build();
                userRepository.save(staff);
                log.info("Đã tạo tài khoản Staff mặc định: staff@gmail.com / staff123");
            }

            // 5. Tạo User Driver
            if (!userRepository.existsByEmail("driver@gmail.com")) {
                User driver = User.builder()
                        .userId("d0000000-0000-0000-0000-000000000001")
                        .email("driver@gmail.com")
                        .fullName("Driver Account")
                        .passwordHash(passwordEncoder.encode("driver123"))
                        .role(roleDriver)
                        .isActive(true)
                        .build();
                userRepository.save(driver);
                log.info("Đã tạo tài khoản Driver mặc định: driver@gmail.com / driver123");
            }
        }
    }

    private Permission createPermission(String id, String description) {
        return permissionRepository.save(Permission.builder()
                .id(id)
                .name(id)
                .description(description)
                .action(id)
                .build());
    }

    private Role createRole(String name, Set<Permission> permissions) {
        return roleRepository.save(Role.builder()
                .name(name)
                .isActive(true)
                .permissions(permissions)
                .build());
    }
}